package com.catrammslib.entity;

import java.io.Serializable;

/**
 * Created by multi on 09.06.18.
 */
public class RequestPerCountryStatistic implements Serializable{

	private String country;
    private Long count;


	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

}
