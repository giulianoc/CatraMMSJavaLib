package com.catrammslib.utility;

import java.io.Serializable;

public class BulkOfDeliveryURLData implements Serializable  {

    private Long mediaItemKey;
    private String uniqueName;
    private Long encodingProfileKey;
	private String encodingProfileLabel;

    private Long liveIngestionJobKey;
    private Long liveDeliveryCode;

	private String deliveryType;
	private Boolean filteredByStatistic;

	// often, it is useful a pointer to the user object referring the mediaItemKey/uniqueName/liveIngestionJobKey
	private Object userReference;

    private String deliveryURL;             // OUT

    public BulkOfDeliveryURLData()
    {
        mediaItemKey = null;
        uniqueName = null;
        encodingProfileKey = null;
        encodingProfileLabel = null;

        liveIngestionJobKey = null;
        liveDeliveryCode = null;

		deliveryType = null;
		filteredByStatistic = null;

		userReference = null;
		
        deliveryURL = null;
    }

    public Long getLiveDeliveryCode() {
        return liveDeliveryCode;
    }

    public void setLiveDeliveryCode(Long liveDeliveryCode) {
        this.liveDeliveryCode = liveDeliveryCode;
    }

    public Boolean getFilteredByStatistic() {
		return filteredByStatistic;
	}

	public void setFilteredByStatistic(Boolean filteredByStatistic) {
		this.filteredByStatistic = filteredByStatistic;
	}

	public Object getUserReference() {
		return userReference;
	}

	public void setUserReference(Object userReference) {
		this.userReference = userReference;
	}

	public String getDeliveryType() {
		return deliveryType;
	}

	public void setDeliveryType(String deliveryType) {
		this.deliveryType = deliveryType;
	}

	public Long getMediaItemKey() {
		return mediaItemKey;
	}

	public void setMediaItemKey(Long mediaItemKey) {
		this.mediaItemKey = mediaItemKey;
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
