package com.launchpad.flow.engine.executors;

import com.launchpad.flow.domain.NodeType;
import com.launchpad.flow.domain.User;
import com.launchpad.flow.domain.WorkflowNode;
import com.launchpad.flow.engine.ExecutionContext;
import com.launchpad.flow.engine.NodeExecutionResult;
import com.launchpad.flow.engine.NodeExecutor;
import com.launchpad.flow.service.HttpJsonClient;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class HttpRequestExecutor implements NodeExecutor {
    private final HttpJsonClient httpJsonClient;

    public HttpRequestExecutor(HttpJsonClient httpJsonClient) {
        this.httpJsonClient = httpJsonClient;
    }

    @Override
    public NodeType supports() {
        return NodeType.HTTP_REQUEST;
    }

    @Override
    public NodeExecutionResult execute(WorkflowNode node, ExecutionContext context, User owner) throws Exception {
        Map<String, Object> config = context.config(node.getConfigJson());
        String method = context.template(config.getOrDefault("method", "GET"));
        String url = context.template(config.get("url"));
        if (url.isBlank()) {
            throw new IllegalArgumentException("HTTP node requires a URL");
        }
        Map<String, String> headers = new LinkedHashMap<>();
        Object rawHeaders = config.get("headers");
        if (rawHeaders instanceof Map<?, ?> map) {
            map.forEach((key, value) -> headers.put(key.toString(), context.template(value)));
        }
        Object body = config.get("body");
        if (body instanceof String text) {
            body = context.template(text);
        }
        return NodeExecutionResult.output(httpJsonClient.sendJson(method, url, headers, body));
    }
}

