package com.launchpad.flow.engine;

import com.launchpad.flow.domain.NodeType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class NodeExecutorRegistry {
    private final Map<NodeType, NodeExecutor> executors = new EnumMap<>(NodeType.class);

    public NodeExecutorRegistry(List<NodeExecutor> executorList) {
        for (NodeExecutor executor : executorList) {
            executors.put(executor.supports(), executor);
        }
    }

    public NodeExecutor get(NodeType nodeType) {
        NodeExecutor executor = executors.get(nodeType);
        if (executor == null) {
            throw new IllegalArgumentException("No executor registered for " + nodeType);
        }
        return executor;
    }
}

