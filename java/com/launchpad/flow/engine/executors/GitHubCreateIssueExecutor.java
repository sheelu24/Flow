package com.launchpad.flow.engine.executors;

import com.launchpad.flow.domain.IntegrationProvider;
import com.launchpad.flow.domain.NodeType;
import com.launchpad.flow.domain.User;
import com.launchpad.flow.domain.WorkflowNode;
import com.launchpad.flow.engine.ExecutionContext;
import com.launchpad.flow.engine.NodeExecutionResult;
import com.launchpad.flow.engine.NodeExecutor;
import com.launchpad.flow.service.GitHubClient;
import com.launchpad.flow.service.IntegrationService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GitHubCreateIssueExecutor implements NodeExecutor {
    private final IntegrationService integrationService;
    private final GitHubClient gitHubClient;

    public GitHubCreateIssueExecutor(IntegrationService integrationService, GitHubClient gitHubClient) {
        this.integrationService = integrationService;
        this.gitHubClient = gitHubClient;
    }

    @Override
    public NodeType supports() {
        return NodeType.GITHUB_CREATE_ISSUE;
    }

    @Override
    public NodeExecutionResult execute(WorkflowNode node, ExecutionContext context, User owner) throws Exception {
        Map<String, Object> config = context.config(node.getConfigJson());
        Long integrationId = config.get("integrationId") == null ? null : Long.valueOf(config.get("integrationId").toString());
        Map<String, Object> credentials = integrationService.credentials(owner, IntegrationProvider.GITHUB, integrationId);
        String ownerName = context.template(config.get("owner"));
        String repo = context.template(config.get("repo"));
        String title = context.template(config.getOrDefault("title", "Workflow issue"));
        String body = context.template(config.getOrDefault("body", ""));
        return NodeExecutionResult.output(gitHubClient.createIssue(credentials, ownerName, repo, title, body, labels(config.get("labels"))));
    }

    private List<String> labels(Object raw) {
        if (raw instanceof List<?> list) {
            List<String> labels = new ArrayList<>();
            list.forEach(item -> labels.add(item.toString()));
            return labels;
        }
        if (raw instanceof String text && !text.isBlank()) {
            return List.of(text.split(","));
        }
        return List.of();
    }
}

