package com.launchpad.flow.service;

import com.launchpad.flow.domain.TriggerType;
import com.launchpad.flow.domain.WorkflowRun;
import com.launchpad.flow.domain.WorkflowStatus;
import com.launchpad.flow.domain.WorkflowTrigger;
import com.launchpad.flow.engine.WorkflowExecutionEngine;
import com.launchpad.flow.repository.WorkflowTriggerRepository;
import java.time.Instant;
import java.util.Map;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduleRunner {
    private final WorkflowTriggerRepository triggerRepository;
    private final WorkflowService workflowService;
    private final WorkflowExecutionEngine executionEngine;

    public ScheduleRunner(
        WorkflowTriggerRepository triggerRepository,
        WorkflowService workflowService,
        WorkflowExecutionEngine executionEngine
    ) {
        this.triggerRepository = triggerRepository;
        this.workflowService = workflowService;
        this.executionEngine = executionEngine;
    }

    @Scheduled(fixedDelayString = "${app.schedules.poll-ms:30000}")
    public void runDueSchedules() {
        Instant now = Instant.now();
        for (WorkflowTrigger trigger : triggerRepository.findByEnabledTrueAndTriggerTypeAndNextRunAtLessThanEqual(TriggerType.SCHEDULE, now)) {
            if (trigger.getWorkflow().getStatus() != WorkflowStatus.PUBLISHED) {
                trigger.setNextRunAt(CronSupport.next(trigger.getCronExpression(), now));
                triggerRepository.save(trigger);
                continue;
            }
            WorkflowRun run = workflowService.createRun(trigger.getWorkflow(), TriggerType.SCHEDULE, Map.of(
                "triggerId", trigger.getId(),
                "scheduledAt", trigger.getNextRunAt().toString()
            ));
            trigger.setNextRunAt(CronSupport.next(trigger.getCronExpression(), now));
            triggerRepository.save(trigger);
            executionEngine.execute(run.getId());
        }
    }
}
