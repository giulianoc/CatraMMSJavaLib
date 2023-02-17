package com.catrammslib.entity;

import java.io.Serializable;

/**
 * Created by multi on 08.06.18.
 */
public class AWSChannelConf implements Serializable{

    private Long confKey;
    private String label;
    private String channelId;
    private String rtmpURL;
    private String playURL;
    private String type;
	private Long outputIndex;
    private Long reservedByIngestionJobKey;
	private String configurationLabel;

    public Long getConfKey() {
        return confKey;
    }

    public void setConfKey(Long confKey) {
        this.confKey = confKey;
    }

	public String getConfigurationLabel() {
		return configurationLabel;
	}

	public void setConfigurationLabel(String configurationLabel) {
		this.configurationLabel = configurationLabel;
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

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getRtmpURL() {
		return rtmpURL;
	}

	public void setRtmpURL(String rtmpURL) {
		this.rtmpURL = rtmpURL;
	}

	public String getPlayURL() {
		return playURL;
	}

	public void setPlayURL(String playURL) {
		this.playURL = playURL;
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
