package com.launchpad.flow.engine;

public record NodeExecutionResult(Object output, Boolean branch) {
    public static NodeExecutionResult output(Object output) {
        return new NodeExecutionResult(output, null);
    }

    public static NodeExecutionResult branch(boolean branch, Object output) {
        return new NodeExecutionResult(output, branch);
    }
}

