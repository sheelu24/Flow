package com.launchpad.flow.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchpad.flow.domain.ExecutionLog;
import com.launchpad.flow.domain.NodeRun;
import com.launchpad.flow.domain.UserIntegration;
import com.launchpad.flow.domain.Workflow;
import com.launchpad.flow.domain.WorkflowEdge;
import com.launchpad.flow.domain.WorkflowNode;
import com.launchpad.flow.domain.WorkflowRun;
import com.launchpad.flow.domain.WorkflowTrigger;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DtoMapper {
    private final ObjectMapper objectMapper;

    public DtoMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> workflow(Workflow workflow) {
        Map<String, Object> result = base(workflow.getId());
        result.put("name", workflow.getName());
        result.put("description", workflow.getDescription());
        result.put("status", workflow.getStatus());
        result.put("createdAt", workflow.getCreatedAt());
        result.put("updatedAt", workflow.getUpdatedAt());
        return result;
    }

    public Map<String, Object> node(WorkflowNode node) {
        Map<String, Object> result = base(node.getId());
        result.put("workflowId", node.getWorkflow().getId());
        result.put("name", node.getName());
        result.put("nodeType", node.getNodeType());
        result.put("config", readJson(node.getConfigJson()));
        result.put("positionX", node.getPositionX());
        result.put("positionY", node.getPositionY());
        result.put("startNode", node.isStartNode());
        return result;
    }

    public Map<String, Object> edge(WorkflowEdge edge) {
        Map<String, Object> result = base(edge.getId());
        result.put("workflowId", edge.getWorkflow().getId());
        result.put("sourceNodeId", edge.getSourceNode().getId());
        result.put("targetNodeId", edge.getTargetNode().getId());
        result.put("edgeType", edge.getEdgeType());
        return result;
    }

    public Map<String, Object> trigger(WorkflowTrigger trigger) {
        Map<String, Object> result = base(trigger.getId());
        result.put("workflowId", trigger.getWorkflow().getId());
        result.put("triggerType", trigger.getTriggerType());
        result.put("webhookPath", trigger.getWebhookPath());
        result.put("webhookSecret", trigger.getWebhookSecret());
        result.put("cronExpression", trigger.getCronExpression());
        result.put("enabled", trigger.isEnabled());
        result.put("nextRunAt", trigger.getNextRunAt());
        return result;
    }

    public Map<String, Object> run(WorkflowRun run) {
        Map<String, Object> result = base(run.getId());
        result.put("workflowId", run.getWorkflow().getId());
        result.put("workflowName", run.getWorkflow().getName());
        result.put("triggerType", run.getTriggerType());
        result.put("status", run.getStatus());
        result.put("inputPayload", readJson(run.getInputPayload()));
        result.put("outputPayload", readJson(run.getOutputPayload()));
        result.put("startedAt", run.getStartedAt());
        result.put("endedAt", run.getEndedAt());
        result.put("errorMessage", run.getErrorMessage());
        return result;
    }

    public Map<String, Object> nodeRun(NodeRun nodeRun) {
        Map<String, Object> result = base(nodeRun.getId());
        result.put("workflowRunId", nodeRun.getWorkflowRun().getId());
        result.put("nodeId", nodeRun.getNode().getId());
        result.put("nodeName", nodeRun.getNode().getName());
        result.put("nodeType", nodeRun.getNode().getNodeType());
        result.put("status", nodeRun.getStatus());
        result.put("inputPayload", readJson(nodeRun.getInputPayload()));
        result.put("outputPayload", readJson(nodeRun.getOutputPayload()));
        result.put("startedAt", nodeRun.getStartedAt());
        result.put("endedAt", nodeRun.getEndedAt());
        result.put("errorMessage", nodeRun.getErrorMessage());
        return result;
    }

    public Map<String, Object> log(ExecutionLog log) {
        Map<String, Object> result = base(log.getId());
        result.put("workflowRunId", log.getWorkflowRun().getId());
        result.put("nodeRunId", log.getNodeRun() == null ? null : log.getNodeRun().getId());
        result.put("logLevel", log.getLogLevel());
        result.put("message", log.getMessage());
        result.put("createdAt", log.getCreatedAt());
        return result;
    }

    public Map<String, Object> integration(UserIntegration integration) {
        Map<String, Object> result = base(integration.getId());
        result.put("provider", integration.getProvider());
        result.put("name", integration.getName());
        result.put("active", integration.isActive());
        result.put("createdAt", integration.getCreatedAt());
        result.put("updatedAt", integration.getUpdatedAt());
        return result;
    }

    private Map<String, Object> base(Long id) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        return result;
    }

    private Object readJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException ex) {
            return json;
        }
    }
}

