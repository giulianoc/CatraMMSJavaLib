package com.catrammslib.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by multi on 09.06.18.
 */
public class EncodingProfile implements Serializable, Comparable<EncodingProfile>{
    private Long encodingProfileKey;
    private boolean global;
    private String label;
    private String contentType;
    private String fileFormat;
    private String description;
    private EncodingProfileVideo videoDetails = new EncodingProfileVideo();
    private EncodingProfileAudio audioDetails = new EncodingProfileAudio();
    private EncodingProfileImage imageDetails = new EncodingProfileImage();

    @Override
    public int compareTo(EncodingProfile o) {
        if (encodingProfileKey != null && o.encodingProfileKey != null)
            return encodingProfileKey.compareTo(o.encodingProfileKey);
        else if (encodingProfileKey == null && o.encodingProfileKey == null)
            return 0;
        else
            return 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EncodingProfile that = (EncodingProfile) o;
        return Objects.equals(encodingProfileKey, that.encodingProfileKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(encodingProfileKey);
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getEncodingProfileKey() {
        return encodingProfileKey;
    }

    public void setEncodingProfileKey(Long encodingProfileKey) {
        this.encodingProfileKey = encodingProfileKey;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public EncodingProfileVideo getVideoDetails() {
        return videoDetails;
    }

    public void setVideoDetails(EncodingProfileVideo videoDetails) {
        this.videoDetails = videoDetails;
    }

    public EncodingProfileAudio getAudioDetails() {
        return audioDetails;
    }

    public void setAudioDetails(EncodingProfileAudio audioDetails) {
        this.audioDetails = audioDetails;
    }

    public EncodingProfileImage getImageDetails() {
        return imageDetails;
    }

    public void setImageDetails(EncodingProfileImage imageDetails) {
        this.imageDetails = imageDetails;
    }

}
