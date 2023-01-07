package com.catrammslib.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by multi on 08.06.18.
 */
public class FacebookConf implements Serializable{

    private Long confKey;
    private String label;
    private String userAccessToken;
	private Date modificationDate;

    public Long getConfKey() {
        return confKey;
    }

    public void setConfKey(Long confKey) {
        this.confKey = confKey;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

	public String getUserAccessToken() {
		return userAccessToken;
	}

	public void setUserAccessToken(String userAccessToken) {
		this.userAccessToken = userAccessToken;
	}

	public Date getModificationDate() {
		return modificationDate;
	}

	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}

}
