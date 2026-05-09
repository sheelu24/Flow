package com.launchpad.flow.repository;

import com.launchpad.flow.domain.ExecutionLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutionLogRepository extends JpaRepository<ExecutionLog, Long> {
    List<ExecutionLog> findByWorkflowRun_IdOrderByCreatedAtAsc(Long workflowRunId);
}
