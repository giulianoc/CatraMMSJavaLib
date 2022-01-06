package com.catrammslib.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by multi on 09.06.18.
 */
public class IngestionWorkflow implements Serializable {

    private Long ingestionRootKey;
    private Long userKey;
    private String userName;
    private String label;
    private Date ingestionDate;
    private Date lastUpdate;
    private String status;
    private List<IngestionJob> ingestionJobList = new ArrayList<>();

    @Override
	public String toString() {
		return "IngestionWorkflow [ingestionDate=" + ingestionDate + ", ingestionJobList=" + ingestionJobList
				+ ", ingestionRootKey=" + ingestionRootKey + ", label=" + label + ", lastUpdate=" + lastUpdate
				+ ", status=" + status + ", userKey=" + userKey + ", userName=" + userName + "]";
	}

	public Long getIngestionRootKey() {
        return ingestionRootKey;
    }

    public void setIngestionRootKey(Long ingestionRootKey) {
        this.ingestionRootKey = ingestionRootKey;
    }

    public Long getUserKey() {
        return userKey;
    }

    public void setUserKey(Long userKey) {
        this.userKey = userKey;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Date getIngestionDate() {
        return ingestionDate;
    }

    public void setIngestionDate(Date ingestionDate) {
        this.ingestionDate = ingestionDate;
    }

    public List<IngestionJob> getIngestionJobList() {
        return ingestionJobList;
    }

    public void setIngestionJobList(List<IngestionJob> ingestionJobList) {
        this.ingestionJobList = ingestionJobList;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
