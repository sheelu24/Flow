package com.launchpad.flow.repository;

import com.launchpad.flow.domain.NodeRun;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NodeRunRepository extends JpaRepository<NodeRun, Long> {
    List<NodeRun> findByWorkflowRun_IdOrderByStartedAtAsc(Long workflowRunId);
}
