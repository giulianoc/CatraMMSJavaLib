package com.catrammslib.entity;

import java.io.Serializable;

/**
 * Created by multi on 09.06.18.
 */
public class EncodingProfileImage implements Serializable {
    private Long width;
    private Long height;
    private Boolean aspectRatio;
    private Long maxWidth;
    private Long maxHeight;
    private String interlaceType;

    public Long getWidth() {
        return width;
    }

    public void setWidth(Long width) {
        this.width = width;
    }

    public Long getHeight() {
        return height;
    }

    public void setHeight(Long height) {
        this.height = height;
    }

    public Boolean getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(Boolean aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public Long getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(Long maxWidth) {
        this.maxWidth = maxWidth;
    }

    public Long getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(Long maxHeight) {
        this.maxHeight = maxHeight;
    }

    public String getInterlaceType() {
        return interlaceType;
    }

    public void setInterlaceType(String interlaceType) {
        this.interlaceType = interlaceType;
    }
}
