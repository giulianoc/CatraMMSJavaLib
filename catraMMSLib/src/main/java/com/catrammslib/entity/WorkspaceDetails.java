package com.catrammslib.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by multi on 08.06.18.
 */
public class WorkspaceDetails implements Serializable {
    private Long workspaceKey;
    private Boolean isEnabled;
    private String name;
    private String maxEncodingPriority;
    private String encodingPeriod;
    private Long maxIngestionsNumber;
    private Long maxStorageInMB;

    private Long usageInMB;
    private Date lastUsageInMBUpdate;

    private String languageCode;
    private Date creationDate;
    private String apiKey;
    private Boolean owner;
    private Boolean defaultWorkspace;
    private Boolean admin;
    private Boolean createRemoveWorkspace;
    private Boolean ingestWorkflow;
    private Boolean createProfiles;
    private Boolean deliveryAuthorization;
    private Boolean shareWorkspace;
    private Boolean editMedia;
    private Boolean editConfiguration;
    private Boolean killEncoding;
    private Boolean cancelIngestionJob;
    private Boolean editEncodersPool;
    private Boolean applicationRecorder;

    // private List<Encoder> encoderList = new ArrayList<>();

    @Override
    public String toString() {
        return "WorkspaceDetails{" +
                "workspaceKey=" + workspaceKey +
                ", isEnabled=" + isEnabled +
                ", name='" + name + '\'' +
                ", maxEncodingPriority='" + maxEncodingPriority + '\'' +
                ", encodingPeriod='" + encodingPeriod + '\'' +
                ", maxIngestionsNumber=" + maxIngestionsNumber +
                ", maxStorageInMB=" + maxStorageInMB +
                ", usageInMB=" + usageInMB +
                ", lastUsageInMBUpdate=" + lastUsageInMBUpdate +
                ", languageCode='" + languageCode + '\'' +
                ", creationDate=" + creationDate +
                ", apiKey='" + apiKey + '\'' +
                ", owner=" + owner +
                ", defaultWorkspace=" + defaultWorkspace +
                ", admin=" + admin +
                ", createRemoveWorkspace=" + createRemoveWorkspace +
                ", ingestWorkflow=" + ingestWorkflow +
                ", createProfiles=" + createProfiles +
                ", deliveryAuthorization=" + deliveryAuthorization +
                ", shareWorkspace=" + shareWorkspace +
                ", editMedia=" + editMedia +
                ", editConfiguration=" + editConfiguration +
                ", killEncoding=" + killEncoding +
                ", cancelIngestionJob=" + cancelIngestionJob +
                ", editEncodersPool=" + editEncodersPool +
                ", applicationRecorder=" + applicationRecorder +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkspaceDetails that = (WorkspaceDetails) o;
        return workspaceKey.equals(that.workspaceKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceKey);
    }

    public Boolean getDefaultWorkspace() {
        return defaultWorkspace;
    }

    public void setDefaultWorkspace(Boolean defaultWorkspace) {
        this.defaultWorkspace = defaultWorkspace;
    }

    public Date getLastUsageInMBUpdate() {
        return lastUsageInMBUpdate;
    }

    public void setLastUsageInMBUpdate(Date lastUsageInMBUpdate) {
        this.lastUsageInMBUpdate = lastUsageInMBUpdate;
    }

    public Long getWorkspaceKey() {
        return workspaceKey;
    }

    public void setWorkspaceKey(Long workspaceKey) {
        this.workspaceKey = workspaceKey;
    }

    public Boolean getOwner() {
        return owner;
    }

    public void setOwner(Boolean owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public Boolean getCreateRemoveWorkspace() {
        return createRemoveWorkspace;
    }

    public void setCreateRemoveWorkspace(Boolean createRemoveWorkspace) {
        this.createRemoveWorkspace = createRemoveWorkspace;
    }

    public Boolean getIngestWorkflow() {
        return ingestWorkflow;
    }

    public void setIngestWorkflow(Boolean ingestWorkflow) {
        this.ingestWorkflow = ingestWorkflow;
    }

    public Boolean getCreateProfiles() {
        return createProfiles;
    }

    public void setCreateProfiles(Boolean createProfiles) {
        this.createProfiles = createProfiles;
    }

    public Boolean getDeliveryAuthorization() {
        return deliveryAuthorization;
    }

    public void setDeliveryAuthorization(Boolean deliveryAuthorization) {
        this.deliveryAuthorization = deliveryAuthorization;
    }

    public Boolean getShareWorkspace() {
        return shareWorkspace;
    }

    public void setShareWorkspace(Boolean shareWorkspace) {
        this.shareWorkspace = shareWorkspace;
    }

    public Boolean getEditMedia() {
        return editMedia;
    }

    public void setEditMedia(Boolean editMedia) {
        this.editMedia = editMedia;
    }

    public Boolean getEnabled() {
        return isEnabled;
    }

    public void setEnabled(Boolean enabled) {
        isEnabled = enabled;
    }

    public String getMaxEncodingPriority() {
        return maxEncodingPriority;
    }

    public void setMaxEncodingPriority(String maxEncodingPriority) {
        this.maxEncodingPriority = maxEncodingPriority;
    }

    public String getEncodingPeriod() {
        return encodingPeriod;
    }

    public void setEncodingPeriod(String encodingPeriod) {
        this.encodingPeriod = encodingPeriod;
    }

    public Long getMaxIngestionsNumber() {
        return maxIngestionsNumber;
    }

    public void setMaxIngestionsNumber(Long maxIngestionsNumber) {
        this.maxIngestionsNumber = maxIngestionsNumber;
    }

    public Long getMaxStorageInMB() {
        return maxStorageInMB;
    }

    public void setMaxStorageInMB(Long maxStorageInMB) {
        this.maxStorageInMB = maxStorageInMB;
    }

    public Long getUsageInMB() {
        return usageInMB;
    }

    public void setUsageInMB(Long usageInMB) {
        this.usageInMB = usageInMB;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Boolean getEditConfiguration() {
        return editConfiguration;
    }

    public void setEditConfiguration(Boolean editConfiguration) {
        this.editConfiguration = editConfiguration;
    }

    public Boolean getKillEncoding() {
        return killEncoding;
    }

    public void setKillEncoding(Boolean killEncoding) {
        this.killEncoding = killEncoding;
    }

    public Boolean getCancelIngestionJob() {
        return cancelIngestionJob;
    }

    public void setCancelIngestionJob(Boolean cancelIngestionJob) {
        this.cancelIngestionJob = cancelIngestionJob;
    }

    public Boolean getEditEncodersPool() {
        return editEncodersPool;
    }

    public void setEditEncodersPool(Boolean editEncodersPool) {
        this.editEncodersPool = editEncodersPool;
    }

    public Boolean getApplicationRecorder() {
        return applicationRecorder;
    }

    public void setApplicationRecorder(Boolean applicationRecorder) {
        this.applicationRecorder = applicationRecorder;
    }

    /*
    public List<Encoder> getEncoderList() {
        return encoderList;
    }

    public void setEncoderList(List<Encoder> encoderList) {
        this.encoderList = encoderList;
    }
     */
}
