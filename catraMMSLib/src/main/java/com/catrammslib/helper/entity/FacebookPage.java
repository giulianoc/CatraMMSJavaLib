package com.catrammslib.helper.entity;

import java.io.Serializable;

/**
 * Created by multi on 13.06.18.
 */
public class FacebookPage implements Serializable {
    private String id;
    private String name;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}
