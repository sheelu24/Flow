package com.launchpad.flow.engine;

import com.launchpad.flow.domain.NodeType;
import com.launchpad.flow.domain.User;
import com.launchpad.flow.domain.WorkflowNode;

public interface NodeExecutor {
    NodeType supports();

    NodeExecutionResult execute(WorkflowNode node, ExecutionContext context, User owner) throws Exception;
}

