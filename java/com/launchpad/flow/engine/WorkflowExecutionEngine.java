package com.launchpad.flow.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchpad.flow.config.AppProperties;
import com.launchpad.flow.domain.EdgeType;
import com.launchpad.flow.domain.ExecutionLog;
import com.launchpad.flow.domain.LogLevel;
import com.launchpad.flow.domain.NodeRun;
import com.launchpad.flow.domain.RunStatus;
import com.launchpad.flow.domain.User;
import com.launchpad.flow.domain.Workflow;
import com.launchpad.flow.domain.WorkflowEdge;
import com.launchpad.flow.domain.WorkflowNode;
import com.launchpad.flow.domain.WorkflowRun;
import com.launchpad.flow.repository.ExecutionLogRepository;
import com.launchpad.flow.repository.NodeRunRepository;
import com.launchpad.flow.repository.WorkflowEdgeRepository;
import com.launchpad.flow.repository.WorkflowNodeRepository;
import com.launchpad.flow.repository.WorkflowRunRepository;
import com.launchpad.flow.service.AppException;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class WorkflowExecutionEngine {
    private final WorkflowRunRepository runRepository;
    private final WorkflowNodeRepository nodeRepository;
    private final WorkflowEdgeRepository edgeRepository;
    private final NodeRunRepository nodeRunRepository;
    private final ExecutionLogRepository logRepository;
    private final NodeExecutorRegistry executorRegistry;
    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;

    public WorkflowExecutionEngine(
        WorkflowRunRepository runRepository,
        WorkflowNodeRepository nodeRepository,
        WorkflowEdgeRepository edgeRepository,
        NodeRunRepository nodeRunRepository,
        ExecutionLogRepository logRepository,
        NodeExecutorRegistry executorRegistry,
        ObjectMapper objectMapper,
        AppProperties appProperties
    ) {
        this.runRepository = runRepository;
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.nodeRunRepository = nodeRunRepository;
        this.logRepository = logRepository;
        this.executorRegistry = executorRegistry;
        this.objectMapper = objectMapper;
        this.appProperties = appProperties;
    }

    public WorkflowRun execute(Long runId) {
        WorkflowRun run = runRepository.findById(runId)
            .orElseThrow(() -> AppException.notFound("Workflow run not found"));
        Workflow workflow = run.getWorkflow();
        User owner = workflow.getOwner();
        List<WorkflowNode> nodes = nodeRepository.findByWorkflow_Id(workflow.getId());
        List<WorkflowEdge> edges = edgeRepository.findByWorkflow_Id(workflow.getId());
        Map<Long, WorkflowNode> nodesById = nodes.stream().collect(Collectors.toMap(WorkflowNode::getId, Function.identity()));
        Map<Long, List<WorkflowEdge>> adjacency = edges.stream().collect(Collectors.groupingBy(edge -> edge.getSourceNode().getId()));

        WorkflowNode current = findStartNode(nodes);
        ExecutionContext context = new ExecutionContext(objectMapper, run.getInputPayload());
        run.setStatus(RunStatus.RUNNING);
        run.setStartedAt(Instant.now());
        runRepository.save(run);
        log(run, null, LogLevel.INFO, "Workflow run started");

        int steps = 0;
        try {
            while (current != null) {
                steps++;
                if (steps > appProperties.getExecution().getMaxSteps()) {
                    throw new IllegalStateException("Workflow exceeded max graph traversal steps");
                }

                NodeExecutionResult result = executeNode(run, current, context, owner);
                context.rememberNode(current.getId().toString(), result.output());
                context.rememberNode(safeKey(current.getName()), result.output());

                current = nextNode(current, result, adjacency, nodesById);
            }
            run.setStatus(RunStatus.SUCCESS);
            run.setOutputPayload(objectMapper.writeValueAsString(context.snapshot()));
            run.setEndedAt(Instant.now());
            runRepository.save(run);
            log(run, null, LogLevel.INFO, "Workflow run completed successfully");
        } catch (Exception exception) {
            run.setStatus(RunStatus.FAILED);
            run.setErrorMessage(exception.getMessage());
            run.setOutputPayload(toJson(context.snapshot()));
            run.setEndedAt(Instant.now());
            runRepository.save(run);
            log(run, null, LogLevel.ERROR, "Workflow run failed: " + exception.getMessage());
        }
        return runRepository.findById(run.getId()).orElse(run);
    }

    private NodeExecutionResult executeNode(WorkflowRun run, WorkflowNode node, ExecutionContext context, User owner) throws Exception {
        NodeRun nodeRun = new NodeRun();
        nodeRun.setWorkflowRun(run);
        nodeRun.setNode(node);
        nodeRun.setStatus(RunStatus.RUNNING);
        nodeRun.setStartedAt(Instant.now());
        nodeRun.setInputPayload(toJson(context.snapshot()));
        nodeRun = nodeRunRepository.save(nodeRun);
        log(run, nodeRun, LogLevel.INFO, "Executing node " + node.getName() + " (" + node.getNodeType() + ")");

        Exception lastFailure = null;
        int maxAttempts = Math.max(1, appProperties.getExecution().getRetryCount() + 1);
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                NodeExecutionResult result = executorRegistry.get(node.getNodeType()).execute(node, context, owner);
                nodeRun.setStatus(RunStatus.SUCCESS);
                nodeRun.setOutputPayload(toJson(result.output()));
                nodeRun.setEndedAt(Instant.now());
                nodeRunRepository.save(nodeRun);
                log(run, nodeRun, LogLevel.INFO, "Node completed in " + millis(nodeRun) + " ms");
                return result;
            } catch (Exception exception) {
                lastFailure = exception;
                log(run, nodeRun, LogLevel.WARN, "Attempt " + attempt + " failed: " + exception.getMessage());
                if (attempt < maxAttempts) {
                    Thread.sleep(250L * attempt);
                }
            }
        }

        nodeRun.setStatus(RunStatus.FAILED);
        nodeRun.setErrorMessage(lastFailure == null ? "Unknown node failure" : lastFailure.getMessage());
        nodeRun.setEndedAt(Instant.now());
        nodeRunRepository.save(nodeRun);
        log(run, nodeRun, LogLevel.ERROR, "Node failed: " + nodeRun.getErrorMessage());
        throw lastFailure == null ? new IllegalStateException("Unknown node failure") : lastFailure;
    }

    private WorkflowNode findStartNode(List<WorkflowNode> nodes) {
        return nodes.stream()
            .filter(WorkflowNode::isStartNode)
            .min(Comparator.comparing(WorkflowNode::getId))
            .orElseThrow(() -> AppException.badRequest("Workflow does not have a start node"));
    }

    private WorkflowNode nextNode(
        WorkflowNode current,
        NodeExecutionResult result,
        Map<Long, List<WorkflowEdge>> adjacency,
        Map<Long, WorkflowNode> nodesById
    ) {
        List<WorkflowEdge> outgoing = adjacency.getOrDefault(current.getId(), List.of());
        if (outgoing.isEmpty()) {
            return null;
        }
        EdgeType preferred = result.branch() == null
            ? EdgeType.DEFAULT
            : result.branch() ? EdgeType.TRUE : EdgeType.FALSE;
        WorkflowEdge edge = outgoing.stream()
            .filter(candidate -> candidate.getEdgeType() == preferred)
            .findFirst()
            .orElseGet(() -> outgoing.stream()
                .filter(candidate -> candidate.getEdgeType() == EdgeType.DEFAULT)
                .findFirst()
                .orElse(outgoing.get(0)));
        return nodesById.get(edge.getTargetNode().getId());
    }

    private void log(WorkflowRun run, NodeRun nodeRun, LogLevel level, String message) {
        ExecutionLog log = new ExecutionLog();
        log.setWorkflowRun(run);
        log.setNodeRun(nodeRun);
        log.setLogLevel(level);
        log.setMessage(message);
        logRepository.save(log);
    }

    private long millis(NodeRun nodeRun) {
        Instant end = nodeRun.getEndedAt() == null ? Instant.now() : nodeRun.getEndedAt();
        return Duration.between(nodeRun.getStartedAt(), end).toMillis();
    }

    private String safeKey(String name) {
        return name == null ? "" : name.trim().replaceAll("[^a-zA-Z0-9_-]+", "_");
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? new LinkedHashMap<>() : value);
        } catch (Exception exception) {
            return "{\"error\":\"Unable to serialize value\"}";
        }
    }
}
