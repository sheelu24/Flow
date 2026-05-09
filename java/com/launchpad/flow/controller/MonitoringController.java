package com.launchpad.flow.controller;

import com.launchpad.flow.config.CurrentUser;
import com.launchpad.flow.domain.RunStatus;
import com.launchpad.flow.domain.WorkflowStatus;
import com.launchpad.flow.dto.DtoMapper;
import com.launchpad.flow.repository.WorkflowRepository;
import com.launchpad.flow.repository.WorkflowRunRepository;
import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MonitoringController {
    private final WorkflowRepository workflowRepository;
    private final WorkflowRunRepository runRepository;
    private final DtoMapper mapper;

    public MonitoringController(
        WorkflowRepository workflowRepository,
        WorkflowRunRepository runRepository,
        DtoMapper mapper
    ) {
        this.workflowRepository = workflowRepository;
        this.runRepository = runRepository;
        this.mapper = mapper;
    }

    @GetMapping("/monitoring/dashboard")
    Object dashboard() {
        Long userId = CurrentUser.require().getId();
        return Map.of(
            "workflows", workflowRepository.countByOwner_Id(userId),
            "published", workflowRepository.countByOwner_IdAndStatus(userId, WorkflowStatus.PUBLISHED),
            "runs", runRepository.countByWorkflow_Owner_Id(userId),
            "successfulRuns", runRepository.countByWorkflow_Owner_IdAndStatus(userId, RunStatus.SUCCESS),
            "failedRuns", runRepository.countByWorkflow_Owner_IdAndStatus(userId, RunStatus.FAILED),
            "recentRuns", runRepository.findTop25ByWorkflow_Owner_IdOrderByStartedAtDesc(userId).stream().map(mapper::run).toList()
        );
    }

    @GetMapping("/health")
    Object health() {
        return Map.of("status", "UP", "time", Instant.now().toString());
    }
}
