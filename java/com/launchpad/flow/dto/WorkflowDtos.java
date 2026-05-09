package com.launchpad.flow.dto;

import com.launchpad.flow.domain.EdgeType;
import com.launchpad.flow.domain.NodeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public final class WorkflowDtos {
    private WorkflowDtos() {
    }

    public record WorkflowRequest(
        @NotBlank String name,
        String description
    ) {
    }

    public record NodeRequest(
        @NotBlank String name,
        @NotNull NodeType nodeType,
        Map<String, Object> config,
        int positionX,
        int positionY,
        boolean startNode
    ) {
    }

    public record EdgeRequest(
        @NotNull Long sourceNodeId,
        @NotNull Long targetNodeId,
        EdgeType edgeType
    ) {
    }

    public record WebhookTriggerRequest(
        String webhookPath,
        String webhookSecret
    ) {
    }

    public record ScheduleTriggerRequest(
        @NotBlank String cronExpression
    ) {
    }

    public record RunRequest(
        Map<String, Object> input
    ) {
    }
}

