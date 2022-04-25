package com.catrammslib.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Created by multi on 09.06.18.
 */
public class RequestPerContentStatistic implements Serializable{

	private String title;
    private Long count;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

}
