package com.catrammslib.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by multi on 07.06.18.
 */
public class MediaItem implements Serializable{
    private Long mediaItemKey;
    private String title;
    private String contentType;
    private String deliveryFileName;
    private Date ingestionDate;
    private Date startPublishing;
    private Date endPublishing;
    private String ingester;
    private String uniqueName;
    private List<String> tags = new ArrayList<>();
    private List<MediaItemCrossReference> crossReferences = new ArrayList<>();
    private String userData;
    private Long retentionInMinutes;
    private Date willBeRemovedAt;

    // this field is filled and used by the GUI
    private String thumbnailURL;
    private Boolean selected;
    private String style;

    public MediaItem()
    {
        selected = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaItem mediaItem = (MediaItem) o;
        return Objects.equals(mediaItemKey, mediaItem.mediaItemKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mediaItemKey);
    }

    private List<PhysicalPath> physicalPathList = new ArrayList<>();

    // this is calculated during fillMediaItem
    private PhysicalPath sourcePhysicalPath;

    public PhysicalPath getSourcePhysicalPath() {
        return sourcePhysicalPath;
    }

    public void setSourcePhysicalPath(PhysicalPath sourcePhysicalPath) {
        this.sourcePhysicalPath = sourcePhysicalPath;
    }

    public List<PhysicalPath> getPhysicalPathList() {
        return physicalPathList;
    }

    public void setPhysicalPathList(List<PhysicalPath> physicalPathList) {
        this.physicalPathList = physicalPathList;
    }

    public Long getMediaItemKey() {
        return mediaItemKey;
    }

    public void setMediaItemKey(Long mediaItemKey) {
        this.mediaItemKey = mediaItemKey;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getDeliveryFileName() {
        return deliveryFileName;
    }

    public void setDeliveryFileName(String deliveryFileName) {
        this.deliveryFileName = deliveryFileName;
    }

    public Date getIngestionDate() {
        return ingestionDate;
    }

    public void setIngestionDate(Date ingestionDate) {
        this.ingestionDate = ingestionDate;
    }

    public Date getStartPublishing() {
        return startPublishing;
    }

    public void setStartPublishing(Date startPublishing) {
        this.startPublishing = startPublishing;
    }

    public Date getEndPublishing() {
        return endPublishing;
    }

    public void setEndPublishing(Date endPublishing) {
        this.endPublishing = endPublishing;
    }

    public String getIngester() {
        return ingester;
    }

    public void setIngester(String ingester) {
        this.ingester = ingester;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Long getRetentionInMinutes() {
        return retentionInMinutes;
    }

    public void setRetentionInMinutes(Long retentionInMinutes) {
        this.retentionInMinutes = retentionInMinutes;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    public List<MediaItemCrossReference> getCrossReferences() {
        return crossReferences;
    }

    public void setCrossReferences(List<MediaItemCrossReference> crossReferences) {
        this.crossReferences = crossReferences;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public Date getWillBeRemovedAt() {
        return willBeRemovedAt;
    }

    public void setWillBeRemovedAt(Date willBeRemovedAt) {
        this.willBeRemovedAt = willBeRemovedAt;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }
}
