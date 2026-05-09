package com.launchpad.flow.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "workflow_edges")
public class WorkflowEdge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "workflow_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Workflow workflow;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "source_node_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private WorkflowNode sourceNode;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "target_node_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private WorkflowNode targetNode;

    @Enumerated(EnumType.STRING)
    private EdgeType edgeType = EdgeType.DEFAULT;

    public Long getId() {
        return id;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public WorkflowNode getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(WorkflowNode sourceNode) {
        this.sourceNode = sourceNode;
    }

    public WorkflowNode getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(WorkflowNode targetNode) {
        this.targetNode = targetNode;
    }

    public EdgeType getEdgeType() {
        return edgeType;
    }

    public void setEdgeType(EdgeType edgeType) {
        this.edgeType = edgeType;
    }
}
