package com.catrammslib.entity;

import java.io.Serializable;

/**
 * Created by multi on 09.06.18.
 */
public class RequestPerUserStatistic implements Serializable{

	private String userId;
    private Long count;


	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}
