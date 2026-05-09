package com.launchpad.flow.service;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GitHubClient {
    private final HttpJsonClient httpJsonClient;

    public GitHubClient(HttpJsonClient httpJsonClient) {
        this.httpJsonClient = httpJsonClient;
    }

    public Map<String, Object> createIssue(
        Map<String, Object> credentials,
        String owner,
        String repo,
        String title,
        String body,
        List<String> labels
    ) throws Exception {
        String resolvedOwner = blankToDefault(owner, credentials.get("defaultOwner"));
        String resolvedRepo = blankToDefault(repo, credentials.get("defaultRepo"));
        if (resolvedOwner.isBlank() || resolvedRepo.isBlank()) {
            throw AppException.badRequest("GitHub owner and repo are required");
        }
        Map<String, Object> request = Map.of(
            "title", title,
            "body", body == null ? "" : body,
            "labels", labels == null ? List.of() : labels
        );
        String url = "https://api.github.com/repos/" + resolvedOwner + "/" + resolvedRepo + "/issues";
        return httpJsonClient.postJson(url, credentials.get("token").toString(), request);
    }

    private String blankToDefault(String value, Object fallback) {
        if (value != null && !value.isBlank()) {
            return value;
        }
        return fallback == null ? "" : fallback.toString();
    }
}

