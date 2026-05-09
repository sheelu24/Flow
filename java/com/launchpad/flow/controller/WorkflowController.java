package com.launchpad.flow.controller;

import com.launchpad.flow.config.CurrentUser;
import com.launchpad.flow.domain.User;
import com.launchpad.flow.dto.DtoMapper;
import com.launchpad.flow.dto.WorkflowDtos.EdgeRequest;
import com.launchpad.flow.dto.WorkflowDtos.NodeRequest;
import com.launchpad.flow.dto.WorkflowDtos.RunRequest;
import com.launchpad.flow.dto.WorkflowDtos.ScheduleTriggerRequest;
import com.launchpad.flow.dto.WorkflowDtos.WebhookTriggerRequest;
import com.launchpad.flow.dto.WorkflowDtos.WorkflowRequest;
import com.launchpad.flow.service.WorkflowService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {
    private final WorkflowService workflowService;
    private final DtoMapper mapper;

    public WorkflowController(WorkflowService workflowService, DtoMapper mapper) {
        this.workflowService = workflowService;
        this.mapper = mapper;
    }

    @GetMapping
    Object list() {
        User user = CurrentUser.require();
        return workflowService.list(user).stream().map(mapper::workflow).toList();
    }

    @PostMapping
    Object create(@Valid @RequestBody WorkflowRequest request) {
        return mapper.workflow(workflowService.create(CurrentUser.require(), request));
    }

    @GetMapping("/{id}")
    Object get(@PathVariable Long id) {
        return mapper.workflow(workflowService.getOwned(CurrentUser.require(), id));
    }

    @PutMapping("/{id}")
    Object update(@PathVariable Long id, @Valid @RequestBody WorkflowRequest request) {
        return mapper.workflow(workflowService.update(CurrentUser.require(), id, request));
    }

    @DeleteMapping("/{id}")
    Map<String, Object> delete(@PathVariable Long id) {
        workflowService.delete(CurrentUser.require(), id);
        return Map.of("deleted", true);
    }

    @PostMapping("/{id}/publish")
    Object publish(@PathVariable Long id) {
        return mapper.workflow(workflowService.publish(CurrentUser.require(), id));
    }

    @PostMapping("/{id}/pause")
    Object pause(@PathVariable Long id) {
        return mapper.workflow(workflowService.pause(CurrentUser.require(), id));
    }

    @PostMapping("/{id}/resume")
    Object resume(@PathVariable Long id) {
        return mapper.workflow(workflowService.resume(CurrentUser.require(), id));
    }

    @PostMapping("/{id}/run")
    Object run(@PathVariable Long id, @RequestBody(required = false) RunRequest request) {
        return mapper.run(workflowService.runManual(CurrentUser.require(), id, request));
    }

    @GetMapping("/{id}/graph")
    Object graph(@PathVariable Long id) {
        User user = CurrentUser.require();
        return Map.of(
            "workflow", mapper.workflow(workflowService.getOwned(user, id)),
            "nodes", workflowService.nodes(user, id).stream().map(mapper::node).toList(),
            "edges", workflowService.edges(user, id).stream().map(mapper::edge).toList(),
            "triggers", workflowService.triggers(user, id).stream().map(mapper::trigger).toList()
        );
    }

    @PostMapping("/{id}/nodes")
    Object addNode(@PathVariable Long id, @Valid @RequestBody NodeRequest request) {
        return mapper.node(workflowService.addNode(CurrentUser.require(), id, request));
    }

    @PutMapping("/{id}/nodes/{nodeId}")
    Object updateNode(@PathVariable Long id, @PathVariable Long nodeId, @Valid @RequestBody NodeRequest request) {
        return mapper.node(workflowService.updateNode(CurrentUser.require(), id, nodeId, request));
    }

    @DeleteMapping("/{id}/nodes/{nodeId}")
    Map<String, Object> deleteNode(@PathVariable Long id, @PathVariable Long nodeId) {
        workflowService.deleteNode(CurrentUser.require(), id, nodeId);
        return Map.of("deleted", true);
    }

    @PostMapping("/{id}/edges")
    Object addEdge(@PathVariable Long id, @Valid @RequestBody EdgeRequest request) {
        return mapper.edge(workflowService.addEdge(CurrentUser.require(), id, request));
    }

    @DeleteMapping("/{id}/edges/{edgeId}")
    Map<String, Object> deleteEdge(@PathVariable Long id, @PathVariable Long edgeId) {
        workflowService.deleteEdge(CurrentUser.require(), id, edgeId);
        return Map.of("deleted", true);
    }

    @PostMapping("/{id}/triggers/webhook")
    Object webhookTrigger(@PathVariable Long id, @RequestBody(required = false) WebhookTriggerRequest request) {
        WebhookTriggerRequest safeRequest = request == null ? new WebhookTriggerRequest(null, null) : request;
        return mapper.trigger(workflowService.createWebhookTrigger(CurrentUser.require(), id, safeRequest));
    }

    @PostMapping("/{id}/triggers/schedule")
    Object scheduleTrigger(@PathVariable Long id, @Valid @RequestBody ScheduleTriggerRequest request) {
        return mapper.trigger(workflowService.createScheduleTrigger(CurrentUser.require(), id, request));
    }

    @GetMapping("/{id}/runs")
    Object runs(@PathVariable Long id) {
        return workflowService.runs(CurrentUser.require(), id).stream().map(mapper::run).toList();
    }
}

