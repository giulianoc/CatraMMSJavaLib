package com.catrammslib.entity;

import netscape.javascript.JSObject;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by multi on 08.06.18.
 */
public class RTMPChannelConf implements Serializable{
    private Long confKey;
    private String label;
    private String rtmpURL;
    private String streamName;
    private String userName;
	private String password;
	private JSONObject playURLDetails;
	private String playURL;
    private String type;
	private Long outputIndex;
    private Long reservedByIngestionJobKey;
	private String configurationLabel;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RTMPChannelConf that = (RTMPChannelConf) o;
		return confKey.equals(that.confKey);
	}

	@Override
	public int hashCode() {
		return Objects.hash(confKey);
	}

	public Long getConfKey() {
        return confKey;
    }

    public void setConfKey(Long confKey) {
        this.confKey = confKey;
    }

	public Long getOutputIndex() {
		return outputIndex;
	}

	public void setOutputIndex(Long outputIndex) {
		this.outputIndex = outputIndex;
	}

	public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

	public String getRtmpURL() {
		return rtmpURL;
	}

	public void setRtmpURL(String rtmpURL) {
		this.rtmpURL = rtmpURL;
	}

	public String getStreamName() {
		return streamName;
	}

	public void setStreamName(String streamName) {
		this.streamName = streamName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getConfigurationLabel() {
		return configurationLabel;
	}

	public void setConfigurationLabel(String configurationLabel) {
		this.configurationLabel = configurationLabel;
	}


	public String getPlayURL() {
		return playURL;
	}

	public void setPlayURL(String playURL) {
		this.playURL = playURL;
	}

	public JSONObject getPlayURLDetails() {
		return playURLDetails;
	}

	public void setPlayURLDetails(JSONObject playURLDetails) {
		this.playURLDetails = playURLDetails;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Long getReservedByIngestionJobKey() {
		return reservedByIngestionJobKey;
	}

	public void setReservedByIngestionJobKey(Long reservedByIngestionJobKey) {
		this.reservedByIngestionJobKey = reservedByIngestionJobKey;
	}

}
