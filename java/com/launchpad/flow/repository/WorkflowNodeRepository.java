package com.launchpad.flow.repository;

import com.launchpad.flow.domain.WorkflowNode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowNodeRepository extends JpaRepository<WorkflowNode, Long> {
    List<WorkflowNode> findByWorkflow_Id(Long workflowId);

    Optional<WorkflowNode> findByIdAndWorkflow_Id(Long id, Long workflowId);

    List<WorkflowNode> findByWorkflow_IdAndStartNodeTrue(Long workflowId);

    void deleteByWorkflow_Id(Long workflowId);
}
