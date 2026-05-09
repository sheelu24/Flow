package com.launchpad.flow.engine.executors;

import com.launchpad.flow.domain.IntegrationProvider;
import com.launchpad.flow.domain.NodeType;
import com.launchpad.flow.domain.User;
import com.launchpad.flow.domain.WorkflowNode;
import com.launchpad.flow.engine.ExecutionContext;
import com.launchpad.flow.engine.NodeExecutionResult;
import com.launchpad.flow.engine.NodeExecutor;
import com.launchpad.flow.service.GmailClient;
import com.launchpad.flow.service.IntegrationService;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GmailSendEmailExecutor implements NodeExecutor {
    private final IntegrationService integrationService;
    private final GmailClient gmailClient;

    public GmailSendEmailExecutor(IntegrationService integrationService, GmailClient gmailClient) {
        this.integrationService = integrationService;
        this.gmailClient = gmailClient;
    }

    @Override
    public NodeType supports() {
        return NodeType.GMAIL_SEND_EMAIL;
    }

    @Override
    public NodeExecutionResult execute(WorkflowNode node, ExecutionContext context, User owner) throws Exception {
        Map<String, Object> config = context.config(node.getConfigJson());
        Long integrationId = config.get("integrationId") == null ? null : Long.valueOf(config.get("integrationId").toString());
        Map<String, Object> credentials = integrationService.credentials(owner, IntegrationProvider.GMAIL, integrationId);
        String to = context.template(config.get("to"));
        String subject = context.template(config.getOrDefault("subject", "Workflow notification"));
        String body = context.template(config.getOrDefault("body", ""));
        return NodeExecutionResult.output(gmailClient.sendEmail(credentials, to, subject, body));
    }
}

