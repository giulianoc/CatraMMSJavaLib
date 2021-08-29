package com.catrammslib.utility;

import java.io.Serializable;

/**
 * Created by multi on 13.06.18.
 */
public class MediaItemReference implements Serializable {

    // option mediaItemKey
    private Long mediaItemKey;

    // option physicalPathKey
    private Long physicalPathKey;

    // option uniqueName
    private String uniqueName;
    private Boolean stopIfReferenceProcessingError;

    // option ingestion label
    private String ingestionLabel;

    // used only in case of mediaItemKey or uniqueName
    private String encodingProfileLabel;
    private Long encodingProfileKey;


    public MediaItemReference()
    {
        mediaItemKey = null;

        physicalPathKey = null;

        uniqueName = null;
        stopIfReferenceProcessingError = false;

        ingestionLabel = null;

        encodingProfileKey = null;
        encodingProfileLabel = null;
    }

    public Long getMediaItemKey() {
        return mediaItemKey;
    }

    public void setMediaItemKey(Long mediaItemKey) {
        this.mediaItemKey = mediaItemKey;
    }

    public String getIngestionLabel() {
        return ingestionLabel;
    }

    public void setIngestionLabel(String ingestionLabel) {
        this.ingestionLabel = ingestionLabel;
    }

    public Long getPhysicalPathKey() {
        return physicalPathKey;
    }

    public void setPhysicalPathKey(Long physicalPathKey) {
        this.physicalPathKey = physicalPathKey;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public Boolean getStopIfReferenceProcessingError() {
        return stopIfReferenceProcessingError;
    }

    public void setStopIfReferenceProcessingError(Boolean stopIfReferenceProcessingError) {
        this.stopIfReferenceProcessingError = stopIfReferenceProcessingError;
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
}
