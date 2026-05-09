package com.launchpad.flow.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExecutionContext {
    private static final Pattern TEMPLATE = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_.-]+)\\s*}}");

    private final ObjectMapper objectMapper;
    private final Map<String, Object> data;

    public ExecutionContext(ObjectMapper objectMapper, String inputPayload) {
        this.objectMapper = objectMapper;
        this.data = new LinkedHashMap<>();
        this.data.put("nodes", new LinkedHashMap<String, Object>());
        this.data.put("last", Map.of());
        try {
            Object input = inputPayload == null || inputPayload.isBlank()
                ? Map.of()
                : objectMapper.readValue(inputPayload, Object.class);
            this.data.put("input", input);
        } catch (Exception exception) {
            this.data.put("input", inputPayload);
        }
    }

    public Map<String, Object> snapshot() {
        return data;
    }

    public void rememberNode(String nodeKey, Object output) {
        nodes().put(nodeKey, output);
        data.put("last", output == null ? Map.of() : output);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> config(String json) {
        if (json == null || json.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception exception) {
            throw new IllegalArgumentException("Node config is not valid JSON");
        }
    }

    public String template(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString();
        Matcher matcher = TEMPLATE.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            Object resolved = getPath(matcher.group(1));
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(resolved == null ? "" : resolved.toString()));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    public Object getPath(String path) {
        String[] parts = path.split("\\.");
        Object current = data;
        for (String part : parts) {
            if (current instanceof Map<?, ?> map) {
                current = map.get(part);
            } else {
                return null;
            }
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> nodes() {
        return (Map<String, Object>) data.get("nodes");
    }
}

