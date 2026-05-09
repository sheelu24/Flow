package com.launchpad.flow.repository;

import com.launchpad.flow.domain.RunStatus;
import com.launchpad.flow.domain.WorkflowRun;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowRunRepository extends JpaRepository<WorkflowRun, Long> {
    List<WorkflowRun> findByWorkflow_IdOrderByStartedAtDesc(Long workflowId);

    List<WorkflowRun> findTop25ByWorkflow_Owner_IdOrderByStartedAtDesc(Long ownerId);

    long countByWorkflow_Owner_Id(Long ownerId);

    long countByWorkflow_Owner_IdAndStatus(Long ownerId, RunStatus status);
}
