package com.catrammslib.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by multi on 08.06.18.
 */
public class PhysicalPath implements Serializable{
    private Long physicalPathKey;
    private Long durationInMilliSeconds;
    private Long bitRate;
    private String fileFormat;
    private String deliveryTechnology;
    private boolean externalReadOnlyStorage;
    private Long partitionNumber;
    private String relativePath;
    private String fileName;
    private Date creationDate;
    private Long encodingProfileKey;
	private String encodingProfileLabel;
    private Long sizeInBytes;
    private String externalDeliveryTechnology;
    private String externalDeliveryURL;
    private Long retentionInMinutes;

    private List<VideoTrack> videoTracks = new ArrayList<>();
    private List<AudioTrack> audioTracks = new ArrayList<>();
    private ImageDetails imageDetails = new ImageDetails();

    // private MediaItem mediaItem;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhysicalPath that = (PhysicalPath) o;
        return Objects.equals(physicalPathKey, that.physicalPathKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(physicalPathKey);
    }

    public Long getDurationInMilliSeconds() {
        return durationInMilliSeconds;
    }

    public void setDurationInMilliSeconds(Long durationInMilliSeconds) {
        this.durationInMilliSeconds = durationInMilliSeconds;
    }

    public Long getBitRate() {
        return bitRate;
    }

    public void setBitRate(Long bitRate) {
        this.bitRate = bitRate;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    public String getDeliveryTechnology() {
        return deliveryTechnology;
    }

    public void setDeliveryTechnology(String deliveryTechnology) {
        this.deliveryTechnology = deliveryTechnology;
    }

    public Long getPhysicalPathKey() {
        return physicalPathKey;
    }

    public void setPhysicalPathKey(Long physicalPathKey) {
        this.physicalPathKey = physicalPathKey;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Long getEncodingProfileKey() {
        return encodingProfileKey;
    }

    public void setEncodingProfileKey(Long encodingProfileKey) {
        this.encodingProfileKey = encodingProfileKey;
    }

    public Long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(Long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

    public boolean isExternalReadOnlyStorage() {
        return externalReadOnlyStorage;
    }

    public void setExternalReadOnlyStorage(boolean externalReadOnlyStorage) {
        this.externalReadOnlyStorage = externalReadOnlyStorage;
    }
/*
    public MediaItem getMediaItem() {
        return mediaItem;
    }

    public void setMediaItem(MediaItem mediaItem) {
        this.mediaItem = mediaItem;
    }
    */

    public List<VideoTrack> getVideoTracks() {
        return videoTracks;
    }

    public void setVideoTracks(List<VideoTrack> videoTracks) {
        this.videoTracks = videoTracks;
    }

    public List<AudioTrack> getAudioTracks() {
        return audioTracks;
    }

    public void setAudioTracks(List<AudioTrack> audioTracks) {
        this.audioTracks = audioTracks;
    }

    public ImageDetails getImageDetails() {
        return imageDetails;
    }

    public void setImageDetails(ImageDetails imageDetails) {
        this.imageDetails = imageDetails;
    }

    public Long getPartitionNumber() {
        return partitionNumber;
    }

    public void setPartitionNumber(Long partitionNumber) {
        this.partitionNumber = partitionNumber;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getExternalDeliveryTechnology() {
        return externalDeliveryTechnology;
    }

    public void setExternalDeliveryTechnology(String externalDeliveryTechnology) {
        this.externalDeliveryTechnology = externalDeliveryTechnology;
    }

    public Long getRetentionInMinutes() {
        return retentionInMinutes;
    }

    public void setRetentionInMinutes(Long retentionInMinutes) {
        this.retentionInMinutes = retentionInMinutes;
    }

    public String getExternalDeliveryURL() {
        return externalDeliveryURL;
    }

    public void setExternalDeliveryURL(String externalDeliveryURL) {
        this.externalDeliveryURL = externalDeliveryURL;
    }

	public String getEncodingProfileLabel() {
		return encodingProfileLabel;
	}

	public void setEncodingProfileLabel(String encodingProfileLabel) {
		this.encodingProfileLabel = encodingProfileLabel;
	}
}
