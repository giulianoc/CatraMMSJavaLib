package com.catrammslib.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Created by multi on 09.06.18.
 */
public class RequestStatistic implements Serializable{
	private Long requestStatisticKey;
    private String userId;
    private Long physicalPathKey;
    private Long confStreamKey;
	private String title;
    private Date requestTimestamp;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((requestStatisticKey == null) ? 0 : requestStatisticKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RequestStatistic other = (RequestStatistic) obj;
		if (requestStatisticKey == null) {
			if (other.requestStatisticKey != null)
				return false;
		} else if (!requestStatisticKey.equals(other.requestStatisticKey))
			return false;
		return true;
	}

	public Long getRequestStatisticKey() {
		return requestStatisticKey;
	}

	public void setRequestStatisticKey(Long requestStatisticKey) {
		this.requestStatisticKey = requestStatisticKey;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public Long getPhysicalPathKey() {
		return physicalPathKey;
	}
	public void setPhysicalPathKey(Long physicalPathKey) {
		this.physicalPathKey = physicalPathKey;
	}
	public Long getConfStreamKey() {
		return confStreamKey;
	}
	public void setConfStreamKey(Long confStreamKey) {
		this.confStreamKey = confStreamKey;
	}
	public Date getRequestTimestamp() {
		return requestTimestamp;
	}
	public void setRequestTimestamp(Date requestTimestamp) {
		this.requestTimestamp = requestTimestamp;
	}
}
