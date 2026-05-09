package com.launchpad.flow.repository;

import com.launchpad.flow.domain.Workflow;
import com.launchpad.flow.domain.WorkflowStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowRepository extends JpaRepository<Workflow, Long> {
    List<Workflow> findByOwner_IdOrderByUpdatedAtDesc(Long ownerId);

    Optional<Workflow> findByIdAndOwner_Id(Long id, Long ownerId);

    long countByOwner_Id(Long ownerId);

    long countByOwner_IdAndStatus(Long ownerId, WorkflowStatus status);
}
