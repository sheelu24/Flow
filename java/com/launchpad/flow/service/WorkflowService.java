package com.launchpad.flow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchpad.flow.domain.EdgeType;
import com.launchpad.flow.domain.NodeType;
import com.launchpad.flow.domain.RunStatus;
import com.launchpad.flow.domain.TriggerType;
import com.launchpad.flow.domain.User;
import com.launchpad.flow.domain.Workflow;
import com.launchpad.flow.domain.WorkflowEdge;
import com.launchpad.flow.domain.WorkflowNode;
import com.launchpad.flow.domain.WorkflowRun;
import com.launchpad.flow.domain.WorkflowStatus;
import com.launchpad.flow.domain.WorkflowTrigger;
import com.launchpad.flow.dto.WorkflowDtos.EdgeRequest;
import com.launchpad.flow.dto.WorkflowDtos.NodeRequest;
import com.launchpad.flow.dto.WorkflowDtos.RunRequest;
import com.launchpad.flow.dto.WorkflowDtos.ScheduleTriggerRequest;
import com.launchpad.flow.dto.WorkflowDtos.WebhookTriggerRequest;
import com.launchpad.flow.dto.WorkflowDtos.WorkflowRequest;
import com.launchpad.flow.engine.WorkflowExecutionEngine;
import com.launchpad.flow.repository.WorkflowEdgeRepository;
import com.launchpad.flow.repository.WorkflowNodeRepository;
import com.launchpad.flow.repository.WorkflowRepository;
import com.launchpad.flow.repository.WorkflowRunRepository;
import com.launchpad.flow.repository.WorkflowTriggerRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkflowService {
    private final WorkflowRepository workflowRepository;
    private final WorkflowNodeRepository nodeRepository;
    private final WorkflowEdgeRepository edgeRepository;
    private final WorkflowTriggerRepository triggerRepository;
    private final WorkflowRunRepository runRepository;
    private final WorkflowExecutionEngine executionEngine;
    private final ObjectMapper objectMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    public WorkflowService(
        WorkflowRepository workflowRepository,
        WorkflowNodeRepository nodeRepository,
        WorkflowEdgeRepository edgeRepository,
        WorkflowTriggerRepository triggerRepository,
        WorkflowRunRepository runRepository,
        WorkflowExecutionEngine executionEngine,
        ObjectMapper objectMapper
    ) {
        this.workflowRepository = workflowRepository;
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.triggerRepository = triggerRepository;
        this.runRepository = runRepository;
        this.executionEngine = executionEngine;
        this.objectMapper = objectMapper;
    }

    public List<Workflow> list(User user) {
        return workflowRepository.findByOwner_IdOrderByUpdatedAtDesc(user.getId());
    }

    public Workflow getOwned(User user, Long id) {
        return workflowRepository.findByIdAndOwner_Id(id, user.getId())
            .orElseThrow(() -> AppException.notFound("Workflow not found"));
    }

    @Transactional
    public Workflow create(User user, WorkflowRequest request) {
        Workflow workflow = new Workflow();
        workflow.setOwner(user);
        workflow.setName(request.name().trim());
        workflow.setDescription(request.description());
        return workflowRepository.save(workflow);
    }

    @Transactional
    public Workflow update(User user, Long id, WorkflowRequest request) {
        Workflow workflow = editable(getOwned(user, id));
        workflow.setName(request.name().trim());
        workflow.setDescription(request.description());
        return workflowRepository.save(workflow);
    }

    @Transactional
    public void delete(User user, Long id) {
        Workflow workflow = getOwned(user, id);
        workflowRepository.delete(workflow);
    }

    public List<WorkflowNode> nodes(User user, Long workflowId) {
        getOwned(user, workflowId);
        return nodeRepository.findByWorkflow_Id(workflowId);
    }

    public List<WorkflowEdge> edges(User user, Long workflowId) {
        getOwned(user, workflowId);
        return edgeRepository.findByWorkflow_Id(workflowId);
    }

    public List<WorkflowTrigger> triggers(User user, Long workflowId) {
        getOwned(user, workflowId);
        return triggerRepository.findByWorkflow_Id(workflowId);
    }

    @Transactional
    public WorkflowNode addNode(User user, Long workflowId, NodeRequest request) {
        Workflow workflow = editable(getOwned(user, workflowId));
        WorkflowNode node = new WorkflowNode();
        node.setWorkflow(workflow);
        applyNodeRequest(node, request);
        return nodeRepository.save(node);
    }

    @Transactional
    public WorkflowNode updateNode(User user, Long workflowId, Long nodeId, NodeRequest request) {
        editable(getOwned(user, workflowId));
        WorkflowNode node = nodeRepository.findByIdAndWorkflow_Id(nodeId, workflowId)
            .orElseThrow(() -> AppException.notFound("Node not found"));
        applyNodeRequest(node, request);
        return nodeRepository.save(node);
    }

    @Transactional
    public void deleteNode(User user, Long workflowId, Long nodeId) {
        editable(getOwned(user, workflowId));
        WorkflowNode node = nodeRepository.findByIdAndWorkflow_Id(nodeId, workflowId)
            .orElseThrow(() -> AppException.notFound("Node not found"));
        nodeRepository.delete(node);
    }

    @Transactional
    public WorkflowEdge addEdge(User user, Long workflowId, EdgeRequest request) {
        Workflow workflow = editable(getOwned(user, workflowId));
        WorkflowNode source = nodeRepository.findByIdAndWorkflow_Id(request.sourceNodeId(), workflowId)
            .orElseThrow(() -> AppException.notFound("Source node not found"));
        WorkflowNode target = nodeRepository.findByIdAndWorkflow_Id(request.targetNodeId(), workflowId)
            .orElseThrow(() -> AppException.notFound("Target node not found"));
        WorkflowEdge edge = new WorkflowEdge();
        edge.setWorkflow(workflow);
        edge.setSourceNode(source);
        edge.setTargetNode(target);
        edge.setEdgeType(request.edgeType() == null ? EdgeType.DEFAULT : request.edgeType());
        return edgeRepository.save(edge);
    }

    @Transactional
    public void deleteEdge(User user, Long workflowId, Long edgeId) {
        editable(getOwned(user, workflowId));
        WorkflowEdge edge = edgeRepository.findByIdAndWorkflow_Id(edgeId, workflowId)
            .orElseThrow(() -> AppException.notFound("Edge not found"));
        edgeRepository.delete(edge);
    }

    @Transactional
    public Workflow publish(User user, Long workflowId) {
        Workflow workflow = getOwned(user, workflowId);
        validateGraph(workflow);
        workflow.setStatus(WorkflowStatus.PUBLISHED);
        return workflowRepository.save(workflow);
    }

    @Transactional
    public Workflow pause(User user, Long workflowId) {
        Workflow workflow = getOwned(user, workflowId);
        workflow.setStatus(WorkflowStatus.PAUSED);
        return workflowRepository.save(workflow);
    }

    @Transactional
    public Workflow resume(User user, Long workflowId) {
        Workflow workflow = getOwned(user, workflowId);
        workflow.setStatus(WorkflowStatus.PUBLISHED);
        return workflowRepository.save(workflow);
    }

    @Transactional
    public WorkflowTrigger createWebhookTrigger(User user, Long workflowId, WebhookTriggerRequest request) {
        Workflow workflow = getOwned(user, workflowId);
        WorkflowTrigger trigger = triggerRepository
            .findByWorkflow_IdAndTriggerType(workflowId, TriggerType.WEBHOOK)
            .orElseGet(WorkflowTrigger::new);
        trigger.setWorkflow(workflow);
        trigger.setTriggerType(TriggerType.WEBHOOK);
        trigger.setWebhookPath(request.webhookPath() == null || request.webhookPath().isBlank() ? randomToken(12) : request.webhookPath());
        trigger.setWebhookSecret(request.webhookSecret() == null || request.webhookSecret().isBlank() ? randomToken(24) : request.webhookSecret());
        trigger.setEnabled(true);
        return triggerRepository.save(trigger);
    }

    @Transactional
    public WorkflowTrigger createScheduleTrigger(User user, Long workflowId, ScheduleTriggerRequest request) {
        Workflow workflow = getOwned(user, workflowId);
        WorkflowTrigger trigger = triggerRepository
            .findByWorkflow_IdAndTriggerType(workflowId, TriggerType.SCHEDULE)
            .orElseGet(WorkflowTrigger::new);
        trigger.setWorkflow(workflow);
        trigger.setTriggerType(TriggerType.SCHEDULE);
        trigger.setCronExpression(request.cronExpression());
        trigger.setNextRunAt(CronSupport.next(request.cronExpression(), Instant.now()));
        trigger.setEnabled(true);
        return triggerRepository.save(trigger);
    }

    @Transactional
    public WorkflowRun createRun(Workflow workflow, TriggerType triggerType, Map<String, Object> input) {
        WorkflowRun run = new WorkflowRun();
        run.setWorkflow(workflow);
        run.setTriggerType(triggerType);
        run.setStatus(RunStatus.QUEUED);
        run.setInputPayload(toJson(input == null ? Map.of() : input));
        return runRepository.save(run);
    }

    public WorkflowRun runManual(User user, Long workflowId, RunRequest request) {
        Workflow workflow = runnable(getOwned(user, workflowId));
        WorkflowRun run = createRun(workflow, TriggerType.MANUAL, request == null ? Map.of() : request.input());
        return executionEngine.execute(run.getId());
    }

    public WorkflowRun runWebhook(WorkflowTrigger trigger, Map<String, Object> input) {
        Workflow workflow = runnable(trigger.getWorkflow());
        WorkflowRun run = createRun(workflow, TriggerType.WEBHOOK, input);
        return executionEngine.execute(run.getId());
    }

    public List<WorkflowRun> runs(User user, Long workflowId) {
        getOwned(user, workflowId);
        return runRepository.findByWorkflow_IdOrderByStartedAtDesc(workflowId);
    }

    private Workflow editable(Workflow workflow) {
        if (workflow.getStatus() != WorkflowStatus.DRAFT) {
            throw AppException.badRequest("Only draft workflows can be edited");
        }
        return workflow;
    }

    private Workflow runnable(Workflow workflow) {
        if (workflow.getStatus() != WorkflowStatus.PUBLISHED) {
            throw AppException.badRequest("Workflow must be published to run");
        }
        return workflow;
    }

    private void applyNodeRequest(WorkflowNode node, NodeRequest request) {
        node.setName(request.name().trim());
        node.setNodeType(request.nodeType());
        node.setPositionX(request.positionX());
        node.setPositionY(request.positionY());
        node.setStartNode(request.startNode());
        node.setConfigJson(toJson(request.config() == null ? Map.of() : request.config()));
    }

    private void validateGraph(Workflow workflow) {
        List<WorkflowNode> nodes = nodeRepository.findByWorkflow_Id(workflow.getId());
        if (nodes.isEmpty()) {
            throw AppException.badRequest("Workflow must contain at least one node");
        }
        long startCount = nodes.stream().filter(WorkflowNode::isStartNode).count();
        if (startCount != 1) {
            throw AppException.badRequest("Workflow must have exactly one start node");
        }
        List<WorkflowEdge> edges = edgeRepository.findByWorkflow_Id(workflow.getId());
        Set<Long> nodeIds = nodes.stream().map(WorkflowNode::getId).collect(java.util.stream.Collectors.toSet());
        for (WorkflowEdge edge : edges) {
            if (!nodeIds.contains(edge.getSourceNode().getId()) || !nodeIds.contains(edge.getTargetNode().getId())) {
                throw AppException.badRequest("Workflow contains an invalid edge");
            }
        }
        for (WorkflowNode node : nodes) {
            if (node.getNodeType() == NodeType.CONDITION) {
                Set<EdgeType> outgoing = edges.stream()
                    .filter(edge -> edge.getSourceNode().getId().equals(node.getId()))
                    .map(WorkflowEdge::getEdgeType)
                    .collect(java.util.stream.Collectors.toSet());
                if (!outgoing.contains(EdgeType.TRUE) || !outgoing.contains(EdgeType.FALSE)) {
                    throw AppException.badRequest("Condition node '" + node.getName() + "' must have TRUE and FALSE edges");
                }
            }
        }
    }

    private String randomToken(int bytes) {
        byte[] buffer = new byte[bytes];
        secureRandom.nextBytes(buffer);
        return HexFormat.of().formatHex(buffer);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw AppException.badRequest("Request contains invalid JSON data");
        }
    }
}
