package com.launchpad.flow.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class HttpJsonClient {
    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(20))
        .build();
    private final ObjectMapper objectMapper;

    public HttpJsonClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> sendJson(
        String method,
        String url,
        Map<String, String> headers,
        Object body
    ) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(40));

        headers.forEach(builder::header);
        String normalized = method == null ? "GET" : method.toUpperCase();
        if ("GET".equals(normalized)) {
            builder.GET();
        } else {
            String json = body == null ? "" : objectMapper.writeValueAsString(body);
            builder.method(normalized, HttpRequest.BodyPublishers.ofString(json));
            builder.header("Content-Type", headers.containsKey("Content-Type") ? headers.get("Content-Type") : "application/json");
        }

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", response.statusCode());
        result.put("headers", response.headers().map());
        result.put("body", tryReadJson(response.body()));
        if (response.statusCode() >= 400) {
            throw AppException.badRequest("HTTP request failed with status " + response.statusCode());
        }
        return result;
    }

    public Map<String, Object> postForm(String url, Map<String, String> form) throws Exception {
        String body = form.entrySet()
            .stream()
            .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
            .collect(Collectors.joining("&"));
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(30))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw AppException.badRequest("OAuth token request failed with status " + response.statusCode());
        }
        return objectMapper.readValue(response.body(), new TypeReference<>() {
        });
    }

    public Map<String, Object> postJson(String url, String bearerToken, Object body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(40))
            .header("Authorization", "Bearer " + bearerToken)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
            .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> parsed = objectMapper.readValue(response.body(), new TypeReference<>() {
        });
        if (response.statusCode() >= 400) {
            throw AppException.badRequest("API request failed with status " + response.statusCode() + ": " + parsed);
        }
        return parsed;
    }

    private Object tryReadJson(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(body, Object.class);
        } catch (Exception exception) {
            return body.length() > 10_000 ? body.substring(0, 10_000) : body;
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}

