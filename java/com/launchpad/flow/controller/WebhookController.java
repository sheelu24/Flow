package com.launchpad.flow.controller;

import com.launchpad.flow.domain.WorkflowRun;
import com.launchpad.flow.domain.WorkflowTrigger;
import com.launchpad.flow.dto.DtoMapper;
import com.launchpad.flow.repository.WorkflowTriggerRepository;
import com.launchpad.flow.service.AppException;
import com.launchpad.flow.service.WorkflowService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hooks")
public class WebhookController {
    private final WorkflowTriggerRepository triggerRepository;
    private final WorkflowService workflowService;
    private final DtoMapper mapper;

    public WebhookController(
        WorkflowTriggerRepository triggerRepository,
        WorkflowService workflowService,
        DtoMapper mapper
    ) {
        this.triggerRepository = triggerRepository;
        this.workflowService = workflowService;
        this.mapper = mapper;
    }

    @PostMapping("/{webhookPath}")
    Object receive(
        @PathVariable String webhookPath,
        @RequestParam(required = false) String secret,
        @RequestBody(required = false) Map<String, Object> body,
        HttpServletRequest request
    ) {
        WorkflowTrigger trigger = triggerRepository.findByWebhookPath(webhookPath)
            .filter(WorkflowTrigger::isEnabled)
            .orElseThrow(() -> AppException.notFound("Webhook not found"));
        String providedSecret = request.getHeader("X-Flow-Secret");
        if (providedSecret == null || providedSecret.isBlank()) {
            providedSecret = secret;
        }
        if (trigger.getWebhookSecret() != null && !trigger.getWebhookSecret().equals(providedSecret)) {
            throw AppException.forbidden("Webhook secret is invalid");
        }
        WorkflowRun run = workflowService.runWebhook(trigger, body == null ? Map.of() : body);
        return mapper.run(run);
    }
}

