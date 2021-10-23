package com.catrammslib.utility;

import java.io.Serializable;

public class BulkOfDeliveryURLData implements Serializable  {

    String uniqueName;
    Long encodingProfileKey;
	String encodingProfileLabel;

    Long liveIngestionJobKey;
    Long liveDeliveryCode;

    String deliveryURL;             // OUT

    public BulkOfDeliveryURLData()
    {
        uniqueName = null;
        encodingProfileKey = null;
        encodingProfileLabel = null;

        liveIngestionJobKey = null;
        liveDeliveryCode = null;

        deliveryURL = null;
    }

    public Long getLiveDeliveryCode() {
        return liveDeliveryCode;
    }

    public void setLiveDeliveryCode(Long liveDeliveryCode) {
        this.liveDeliveryCode = liveDeliveryCode;
    }

    public String getUniqueName() {
		return uniqueName;
	}

	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	public String getEncodingProfileLabel() {
		return encodingProfileLabel;
	}

	public void setEncodingProfileLabel(String encodingProfileLabel) {
		this.encodingProfileLabel = encodingProfileLabel;
	}

	public Long getEncodingProfileKey() {
		return encodingProfileKey;
	}

	public void setEncodingProfileKey(Long encodingProfileKey) {
		this.encodingProfileKey = encodingProfileKey;
	}

	public Long getLiveIngestionJobKey() {
        return liveIngestionJobKey;
    }

    public void setLiveIngestionJobKey(Long liveIngestionJobKey) {
        this.liveIngestionJobKey = liveIngestionJobKey;
    }

    public String getDeliveryURL() {
        return deliveryURL;
    }

    public void setDeliveryURL(String deliveryURL) {
        this.deliveryURL = deliveryURL;
    }
}
