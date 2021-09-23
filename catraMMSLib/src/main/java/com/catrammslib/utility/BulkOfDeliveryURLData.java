package com.catrammslib.utility;

import java.io.Serializable;

public class BulkOfDeliveryURLData implements Serializable  {

    String vodUniqueName;
    Long vodEncodingProfileKey;

    Long liveIngestionJobKey;
    Long liveDeliveryCode;

    String deliveryURL;             // OUT

    public BulkOfDeliveryURLData()
    {
        vodUniqueName = null;
        vodEncodingProfileKey = null;

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

    public String getVodUniqueName() {
        return vodUniqueName;
    }

    public void setVodUniqueName(String vodUniqueName) {
        this.vodUniqueName = vodUniqueName;
    }

    public Long getVodEncodingProfileKey() {
        return vodEncodingProfileKey;
    }

    public void setVodEncodingProfileKey(Long vodEncodingProfileKey) {
        this.vodEncodingProfileKey = vodEncodingProfileKey;
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
