package com.catrammslib.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by multi on 08.06.18.
 */
public class WorkflowLibrary implements Serializable{

    private Long workflowLibraryKey;
    private Boolean global;
    private Long creatorUserKey;    // valid only in case global is false
    private String label;
    private Long thumbnailMediaItemKey;
    private List<WorkflowVariable> workflowVariableList = new ArrayList<>();


    public WorkflowLibrary clone()
    {
        WorkflowLibrary workflowLibrary = new WorkflowLibrary();
        workflowLibrary.setThumbnailMediaItemKey(getThumbnailMediaItemKey());
        workflowLibrary.setGlobal(getGlobal());
        workflowLibrary.setCreatorUserKey(getCreatorUserKey());
        workflowLibrary.setWorkflowLibraryKey(getWorkflowLibraryKey());
        workflowLibrary.setLabel(getLabel());

        workflowLibrary.setWorkflowVariableList(new ArrayList<>());
        for (WorkflowVariable workflowVariable: getWorkflowVariableList())
            workflowLibrary.getWorkflowVariableList().add(workflowVariable.clone());

        return workflowLibrary;
    }

    public Long getWorkflowLibraryKey() {
        return workflowLibraryKey;
    }

    public void setWorkflowLibraryKey(Long workflowLibraryKey) {
        this.workflowLibraryKey = workflowLibraryKey;
    }

    public Boolean getGlobal() {
        return global;
    }

    public void setGlobal(Boolean global) {
        this.global = global;
    }

    public String getLabel() {
        return label;
    }

    public Long getCreatorUserKey() {
        return creatorUserKey;
    }

    public void setCreatorUserKey(Long creatorUserKey) {
        this.creatorUserKey = creatorUserKey;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getThumbnailMediaItemKey() {
        return thumbnailMediaItemKey;
    }

    public void setThumbnailMediaItemKey(Long thumbnailMediaItemKey) {
        this.thumbnailMediaItemKey = thumbnailMediaItemKey;
    }

    public List<WorkflowVariable> getWorkflowVariableList() {
        return workflowVariableList;
    }

    public void setWorkflowVariableList(List<WorkflowVariable> workflowVariableList) {
        this.workflowVariableList = workflowVariableList;
    }
}
