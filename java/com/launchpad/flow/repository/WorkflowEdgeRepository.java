package com.launchpad.flow.repository;

import com.launchpad.flow.domain.WorkflowEdge;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowEdgeRepository extends JpaRepository<WorkflowEdge, Long> {
    List<WorkflowEdge> findByWorkflow_Id(Long workflowId);

    List<WorkflowEdge> findByWorkflow_IdAndSourceNode_Id(Long workflowId, Long sourceNodeId);

    Optional<WorkflowEdge> findByIdAndWorkflow_Id(Long id, Long workflowId);

    void deleteByWorkflow_Id(Long workflowId);
}
