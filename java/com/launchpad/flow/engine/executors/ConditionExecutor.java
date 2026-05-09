package com.launchpad.flow.engine.executors;

import com.launchpad.flow.domain.NodeType;
import com.launchpad.flow.domain.User;
import com.launchpad.flow.domain.WorkflowNode;
import com.launchpad.flow.engine.ExecutionContext;
import com.launchpad.flow.engine.NodeExecutionResult;
import com.launchpad.flow.engine.NodeExecutor;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ConditionExecutor implements NodeExecutor {
    @Override
    public NodeType supports() {
        return NodeType.CONDITION;
    }

    @Override
    public NodeExecutionResult execute(WorkflowNode node, ExecutionContext context, User owner) {
        Map<String, Object> config = context.config(node.getConfigJson());
        String path = config.getOrDefault("path", "input").toString();
        String operator = config.getOrDefault("operator", "exists").toString();
        Object expected = config.get("value");
        Object actual = context.getPath(path);
        boolean matched = evaluate(actual, operator, expected);
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("path", path);
        output.put("operator", operator);
        output.put("expected", expected);
        output.put("actual", actual);
        output.put("matched", matched);
        return NodeExecutionResult.branch(matched, output);
    }

    private boolean evaluate(Object actual, String operator, Object expected) {
        return switch (operator) {
            case "equals" -> String.valueOf(actual).equals(String.valueOf(expected));
            case "not_equals" -> !String.valueOf(actual).equals(String.valueOf(expected));
            case "gt" -> number(actual).compareTo(number(expected)) > 0;
            case "gte" -> number(actual).compareTo(number(expected)) >= 0;
            case "lt" -> number(actual).compareTo(number(expected)) < 0;
            case "lte" -> number(actual).compareTo(number(expected)) <= 0;
            case "contains" -> actual != null && expected != null && actual.toString().contains(expected.toString());
            case "exists" -> actual != null;
            default -> throw new IllegalArgumentException("Unsupported condition operator: " + operator);
        };
    }

    private BigDecimal number(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.toString());
    }
}
