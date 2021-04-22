package com.catrammslib.utility;

import java.io.Serializable;

/**
 * Created by multi on 13.06.18.
 */
public class MediaItemKeyReference implements Serializable {
    private Long mediaItemKey;
    private String encodingProfileLabel;
    private Long encodingProfileKey;


    public MediaItemKeyReference()
    {
        encodingProfileKey = null;
        encodingProfileLabel = null;
    }

    public Long getMediaItemKey() {
        return mediaItemKey;
    }

    public void setMediaItemKey(Long mediaItemKey) {
        this.mediaItemKey = mediaItemKey;
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
