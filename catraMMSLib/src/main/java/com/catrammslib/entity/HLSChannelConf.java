package com.catrammslib.entity;

import java.io.Serializable;

/**
 * Created by multi on 08.06.18.
 */
public class HLSChannelConf implements Serializable{
    private Long confKey;
    private String label;
    private Long deliveryCode;
    private Long segmentDuration;
    private Long playlistEntriesNumber;
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

	public String getConfigurationLabel() {
		return configurationLabel;
	}

	public void setConfigurationLabel(String configurationLabel) {
		this.configurationLabel = configurationLabel;
	}

	public Long getDeliveryCode() {
		return deliveryCode;
	}

	public void setDeliveryCode(Long deliveryCode) {
		this.deliveryCode = deliveryCode;
	}

	public Long getSegmentDuration() {
		return segmentDuration;
	}

	public void setSegmentDuration(Long segmentDuration) {
		this.segmentDuration = segmentDuration;
	}

	public Long getPlaylistEntriesNumber() {
		return playlistEntriesNumber;
	}

	public void setPlaylistEntriesNumber(Long playlistEntriesNumber) {
		this.playlistEntriesNumber = playlistEntriesNumber;
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
