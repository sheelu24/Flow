package com.launchpad.flow.controller;

import com.launchpad.flow.config.CurrentUser;
import com.launchpad.flow.domain.WorkflowRun;
import com.launchpad.flow.dto.DtoMapper;
import com.launchpad.flow.repository.ExecutionLogRepository;
import com.launchpad.flow.repository.NodeRunRepository;
import com.launchpad.flow.repository.WorkflowRunRepository;
import com.launchpad.flow.service.AppException;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/runs")
public class RunController {
    private final WorkflowRunRepository runRepository;
    private final NodeRunRepository nodeRunRepository;
    private final ExecutionLogRepository logRepository;
    private final DtoMapper mapper;

    public RunController(
        WorkflowRunRepository runRepository,
        NodeRunRepository nodeRunRepository,
        ExecutionLogRepository logRepository,
        DtoMapper mapper
    ) {
        this.runRepository = runRepository;
        this.nodeRunRepository = nodeRunRepository;
        this.logRepository = logRepository;
        this.mapper = mapper;
    }

    @GetMapping("/{runId}")
    Object get(@PathVariable Long runId) {
        WorkflowRun run = ownedRun(runId);
        return Map.of(
            "run", mapper.run(run),
            "nodeRuns", nodeRunRepository.findByWorkflowRun_IdOrderByStartedAtAsc(runId).stream().map(mapper::nodeRun).toList(),
            "logs", logRepository.findByWorkflowRun_IdOrderByCreatedAtAsc(runId).stream().map(mapper::log).toList()
        );
    }

    @GetMapping("/{runId}/logs")
    Object logs(@PathVariable Long runId) {
        ownedRun(runId);
        return logRepository.findByWorkflowRun_IdOrderByCreatedAtAsc(runId).stream().map(mapper::log).toList();
    }

    private WorkflowRun ownedRun(Long runId) {
        Long userId = CurrentUser.require().getId();
        return runRepository.findById(runId)
            .filter(run -> run.getWorkflow().getOwner().getId().equals(userId))
            .orElseThrow(() -> AppException.notFound("Run not found"));
    }
}
