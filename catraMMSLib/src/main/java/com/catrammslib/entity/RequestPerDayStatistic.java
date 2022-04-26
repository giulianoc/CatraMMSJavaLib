package com.catrammslib.entity;

import java.io.Serializable;

/**
 * Created by multi on 09.06.18.
 */
public class RequestPerDayStatistic implements Serializable{

	private String date;
    private Long count;


	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

}
