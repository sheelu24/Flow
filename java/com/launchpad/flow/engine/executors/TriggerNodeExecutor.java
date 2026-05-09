package com.launchpad.flow.engine.executors;

import com.launchpad.flow.domain.NodeType;
import com.launchpad.flow.domain.User;
import com.launchpad.flow.domain.WorkflowNode;
import com.launchpad.flow.engine.ExecutionContext;
import com.launchpad.flow.engine.NodeExecutionResult;
import com.launchpad.flow.engine.NodeExecutor;
import java.util.Map;

public abstract class TriggerNodeExecutor implements NodeExecutor {
    @Override
    public NodeExecutionResult execute(WorkflowNode node, ExecutionContext context, User owner) {
        return NodeExecutionResult.output(Map.of("triggered", true, "nodeType", node.getNodeType()));
    }

    public static class Webhook extends TriggerNodeExecutor {
        @Override
        public NodeType supports() {
            return NodeType.WEBHOOK_TRIGGER;
        }
    }

    public static class Schedule extends TriggerNodeExecutor {
        @Override
        public NodeType supports() {
            return NodeType.SCHEDULE_TRIGGER;
        }
    }

    public static class Manual extends TriggerNodeExecutor {
        @Override
        public NodeType supports() {
            return NodeType.MANUAL_TRIGGER;
        }
    }
}

