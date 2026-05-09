package com.launchpad.flow.repository;

import com.launchpad.flow.domain.TriggerType;
import com.launchpad.flow.domain.WorkflowTrigger;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowTriggerRepository extends JpaRepository<WorkflowTrigger, Long> {
    List<WorkflowTrigger> findByWorkflow_Id(Long workflowId);

    Optional<WorkflowTrigger> findByWebhookPath(String webhookPath);

    Optional<WorkflowTrigger> findByWorkflow_IdAndTriggerType(Long workflowId, TriggerType triggerType);

    List<WorkflowTrigger> findByEnabledTrueAndTriggerTypeAndNextRunAtLessThanEqual(TriggerType triggerType, Instant now);
}
