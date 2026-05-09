package com.launchpad.flow.engine.executors;

import com.launchpad.flow.config.AppProperties;
import com.launchpad.flow.domain.NodeType;
import com.launchpad.flow.domain.User;
import com.launchpad.flow.domain.WorkflowNode;
import com.launchpad.flow.engine.ExecutionContext;
import com.launchpad.flow.engine.NodeExecutionResult;
import com.launchpad.flow.engine.NodeExecutor;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DelayExecutor implements NodeExecutor {
    private final AppProperties appProperties;

    public DelayExecutor(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public NodeType supports() {
        return NodeType.DELAY;
    }

    @Override
    public NodeExecutionResult execute(WorkflowNode node, ExecutionContext context, User owner) throws InterruptedException {
        Map<String, Object> config = context.config(node.getConfigJson());
        int seconds = Integer.parseInt(config.getOrDefault("seconds", 1).toString());
        int capped = Math.min(Math.max(seconds, 0), appProperties.getExecution().getMaxDelaySeconds());
        Thread.sleep(capped * 1000L);
        return NodeExecutionResult.output(Map.of("requestedSeconds", seconds, "waitedSeconds", capped));
    }
}

