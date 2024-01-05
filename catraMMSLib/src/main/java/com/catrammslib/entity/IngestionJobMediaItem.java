package com.catrammslib.entity;

import java.io.Serializable;

/**
 * Created by multi on 09.06.18.
 */
public class IngestionJobMediaItem implements Serializable {
    private Long mediaItemKey;
    private Long physicalPathKey;
    private Long position;

    public Long getMediaItemKey() {
        return mediaItemKey;
    }

    public void setMediaItemKey(Long mediaItemKey) {
        this.mediaItemKey = mediaItemKey;
    }

    public Long getPosition() {
        return position;
    }

    public void setPosition(Long position) {
        this.position = position;
    }

    public Long getPhysicalPathKey() {
        return physicalPathKey;
    }

    public void setPhysicalPathKey(Long physicalPathKey) {
        this.physicalPathKey = physicalPathKey;
    }
}
