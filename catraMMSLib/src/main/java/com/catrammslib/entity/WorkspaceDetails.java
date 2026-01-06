package com.catrammslib.entity;

import com.catrammslib.utility.Cost;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by multi on 08.06.18.
 */
public class WorkspaceDetails implements Serializable {
    private Long workspaceKey;
    private Boolean enabled;
    private String name;
    private String notes;
    private String maxEncodingPriority;
    private String encodingPeriod;
    private Long maxIngestionsNumber;

    private Long maxStorageInGB;
    private Long currentCostForStorage;
    private Long dedicatedEncoder_power_1;
    private Long currentCostForDedicatedEncoder_power_1;
    private Long dedicatedEncoder_power_2;
    private Long currentCostForDedicatedEncoder_power_2;
    private Long dedicatedEncoder_power_3;
    private Long currentCostForDedicatedEncoder_power_3;
    private Long CDN_type_1;
    private Long currentCostForCDN_type_1;
    private Boolean support_type_1;
    private Long currentCostForSupport_type_1;



    private Long workspaceOwnerUserKey;	// filled only if user is admin
	private String workspaceOwnerUserName; // filled only if user is admin

    private Long usageInMB;
    private Date lastUsageInMBUpdate;

    private String languageCode;
    private String timezone;
    private JSONObject preferences;
    private JSONObject externalDeliveries;
    private Date creationDate;
    private String apiKey;
    private Boolean owner;
    private Boolean defaultWorkspace;
    private Date expirationDate;
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
    private Boolean createRemoveLiveChannel;
    private Boolean updateEncoderStats;


    // this field is used by the GUI
    private List<Cost> dedicatedResources;
    private Long currentTotalCost;
    private Long newTotalCost;
    private Long differenceBetweenCurrentAndNewForStorage;
    private Long differenceBetweenCurrentAndNewForDedicatedEncoder_power_1;
    private Long differenceBetweenCurrentAndNewForDedicatedEncoder_power_2;
    private Long differenceBetweenCurrentAndNewForDedicatedEncoder_power_3;
    private Long differenceBetweenCurrentAndNewForCDN_type_1;
    private Long differenceBetweenCurrentAndNewForSupport_type_1;


    @Override
    public String toString() {
        return "WorkspaceDetails{" +
                "workspaceKey=" + workspaceKey +
                ", enabled=" + enabled +
                ", name='" + name + '\'' +
                ", notes='" + notes + '\'' +
                ", maxEncodingPriority='" + maxEncodingPriority + '\'' +
                ", encodingPeriod='" + encodingPeriod + '\'' +
                ", maxIngestionsNumber=" + maxIngestionsNumber +
                ", workspaceOwnerUserKey=" + workspaceOwnerUserKey +
                ", workspaceOwnerUserName=" + workspaceOwnerUserName +
                ", usageInMB=" + usageInMB +
                ", lastUsageInMBUpdate=" + lastUsageInMBUpdate +
                ", languageCode='" + languageCode + '\'' +
                ", timezone='" + timezone + '\'' +
                ", preferences='" + (preferences != null ? preferences.toString() : "null") + '\'' +
                ", externalDeliveries='" + (externalDeliveries != null ? externalDeliveries.toString() : "null") + '\'' +
                ", creationDate=" + creationDate +
                ", apiKey='" + apiKey + '\'' +
                ", owner=" + owner +
                ", defaultWorkspace=" + defaultWorkspace +
                ", expirationDate=" + expirationDate +
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
                ", createRemoveLiveChannel=" + createRemoveLiveChannel +
                ", updateEncoderStats=" + updateEncoderStats +

                ", maxStorageInGB=" + maxStorageInGB +
                ", currentCostForStorage=" + currentCostForStorage +
                ", dedicatedEncoder_power_1=" + dedicatedEncoder_power_1 +
                ", currentCostForDedicatedEncoder_power_1=" + currentCostForDedicatedEncoder_power_1 +
                ", dedicatedEncoder_power_2=" + dedicatedEncoder_power_2 +
                ", currentCostForDedicatedEncoder_power_2=" + currentCostForDedicatedEncoder_power_2 +
                ", dedicatedEncoder_power_3=" + dedicatedEncoder_power_3 +
                ", currentCostForDedicatedEncoder_power_3=" + currentCostForDedicatedEncoder_power_3 +
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public Boolean getUpdateEncoderStats() {
        return updateEncoderStats;
    }

    public void setUpdateEncoderStats(Boolean updateEncoderStats) {
        this.updateEncoderStats = updateEncoderStats;
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
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Long getWorkspaceOwnerUserKey() {
		return workspaceOwnerUserKey;
	}

	public void setWorkspaceOwnerUserKey(Long workspaceOwnerUserKey) {
		this.workspaceOwnerUserKey = workspaceOwnerUserKey;
	}

	public String getWorkspaceOwnerUserName() {
		return workspaceOwnerUserName;
	}

	public void setWorkspaceOwnerUserName(String workspaceOwnerUserName) {
		this.workspaceOwnerUserName = workspaceOwnerUserName;
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

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public JSONObject getPreferences() {
        return preferences;
    }

    public void setPreferences(JSONObject preferences) {
        this.preferences = preferences;
    }

    public JSONObject getExternalDeliveries() {
        return externalDeliveries;
    }

    public void setExternalDeliveries(JSONObject externalDeliveries) {
        this.externalDeliveries = externalDeliveries;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
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

    public Long getMaxStorageInGB() {
        return maxStorageInGB;
    }

    public void setMaxStorageInGB(Long maxStorageInGB) {
        this.maxStorageInGB = maxStorageInGB;
    }

    public Long getCurrentCostForStorage() {
        return currentCostForStorage;
    }

    public void setCurrentCostForStorage(Long currentCostForStorage) {
        this.currentCostForStorage = currentCostForStorage;
    }

    public Long getDedicatedEncoder_power_1() {
        return dedicatedEncoder_power_1;
    }

    public void setDedicatedEncoder_power_1(Long dedicatedEncoder_power_1) {
        this.dedicatedEncoder_power_1 = dedicatedEncoder_power_1;
    }

    public Long getCurrentCostForDedicatedEncoder_power_1() {
        return currentCostForDedicatedEncoder_power_1;
    }

    public void setCurrentCostForDedicatedEncoder_power_1(Long currentCostForDedicatedEncoder_power_1) {
        this.currentCostForDedicatedEncoder_power_1 = currentCostForDedicatedEncoder_power_1;
    }

    public Long getDedicatedEncoder_power_2() {
        return dedicatedEncoder_power_2;
    }

    public void setDedicatedEncoder_power_2(Long dedicatedEncoder_power_2) {
        this.dedicatedEncoder_power_2 = dedicatedEncoder_power_2;
    }

    public Long getCurrentCostForDedicatedEncoder_power_2() {
        return currentCostForDedicatedEncoder_power_2;
    }

    public void setCurrentCostForDedicatedEncoder_power_2(Long currentCostForDedicatedEncoder_power_2) {
        this.currentCostForDedicatedEncoder_power_2 = currentCostForDedicatedEncoder_power_2;
    }

    public Long getDedicatedEncoder_power_3() {
        return dedicatedEncoder_power_3;
    }

    public void setDedicatedEncoder_power_3(Long dedicatedEncoder_power_3) {
        this.dedicatedEncoder_power_3 = dedicatedEncoder_power_3;
    }

    public Long getCurrentCostForDedicatedEncoder_power_3() {
        return currentCostForDedicatedEncoder_power_3;
    }

    public void setCurrentCostForDedicatedEncoder_power_3(Long currentCostForDedicatedEncoder_power_3) {
        this.currentCostForDedicatedEncoder_power_3 = currentCostForDedicatedEncoder_power_3;
    }

    public Boolean getApplicationRecorder() {
        return applicationRecorder;
    }

    public void setApplicationRecorder(Boolean applicationRecorder) {
        this.applicationRecorder = applicationRecorder;
    }

    public Boolean getCreateRemoveLiveChannel() {
        return createRemoveLiveChannel;
    }

    public void setCreateRemoveLiveChannel(Boolean createRemoveLiveChannel) {
        this.createRemoveLiveChannel = createRemoveLiveChannel;
    }

    public Long getCurrentTotalCost() {
        return currentTotalCost;
    }

    public void setCurrentTotalCost(Long currentTotalCost) {
        this.currentTotalCost = currentTotalCost;
    }

    public Long getNewTotalCost() {
        return newTotalCost;
    }

    public void setNewTotalCost(Long newTotalCost) {
        this.newTotalCost = newTotalCost;
    }

    public List<Cost> getDedicatedResources() {
        return dedicatedResources;
    }

    public void setDedicatedResources(List<Cost> dedicatedResources) {
        this.dedicatedResources = dedicatedResources;
    }

    public Long getDifferenceBetweenCurrentAndNewForCDN_type_1() {
        return differenceBetweenCurrentAndNewForCDN_type_1;
    }

    public void setDifferenceBetweenCurrentAndNewForCDN_type_1(Long differenceBetweenCurrentAndNewForCDN_type_1) {
        this.differenceBetweenCurrentAndNewForCDN_type_1 = differenceBetweenCurrentAndNewForCDN_type_1;
    }

    public Long getDifferenceBetweenCurrentAndNewForStorage() {
        return differenceBetweenCurrentAndNewForStorage;
    }

    public void setDifferenceBetweenCurrentAndNewForStorage(Long differenceBetweenCurrentAndNewForStorage) {
        this.differenceBetweenCurrentAndNewForStorage = differenceBetweenCurrentAndNewForStorage;
    }

    public Long getDifferenceBetweenCurrentAndNewForDedicatedEncoder_power_1() {
        return differenceBetweenCurrentAndNewForDedicatedEncoder_power_1;
    }

    public void setDifferenceBetweenCurrentAndNewForDedicatedEncoder_power_1(Long differenceBetweenCurrentAndNewForDedicatedEncoder_power_1) {
        this.differenceBetweenCurrentAndNewForDedicatedEncoder_power_1 = differenceBetweenCurrentAndNewForDedicatedEncoder_power_1;
    }

    public Long getDifferenceBetweenCurrentAndNewForDedicatedEncoder_power_2() {
        return differenceBetweenCurrentAndNewForDedicatedEncoder_power_2;
    }

    public void setDifferenceBetweenCurrentAndNewForDedicatedEncoder_power_2(Long differenceBetweenCurrentAndNewForDedicatedEncoder_power_2) {
        this.differenceBetweenCurrentAndNewForDedicatedEncoder_power_2 = differenceBetweenCurrentAndNewForDedicatedEncoder_power_2;
    }

    public Boolean getSupport_type_1() {
        return support_type_1;
    }

    public void setSupport_type_1(Boolean support_type_1) {
        this.support_type_1 = support_type_1;
    }

    public Long getCurrentCostForSupport_type_1() {
        return currentCostForSupport_type_1;
    }

    public void setCurrentCostForSupport_type_1(Long currentCostForSupport_type_1) {
        this.currentCostForSupport_type_1 = currentCostForSupport_type_1;
    }

    public Long getCDN_type_1() {
        return CDN_type_1;
    }

    public void setCDN_type_1(Long CDN_type_1) {
        this.CDN_type_1 = CDN_type_1;
    }

    public Long getCurrentCostForCDN_type_1() {
        return currentCostForCDN_type_1;
    }

    public void setCurrentCostForCDN_type_1(Long currentCostForCDN_type_1) {
        this.currentCostForCDN_type_1 = currentCostForCDN_type_1;
    }

    public Long getDifferenceBetweenCurrentAndNewForSupport_type_1() {
        return differenceBetweenCurrentAndNewForSupport_type_1;
    }

    public void setDifferenceBetweenCurrentAndNewForSupport_type_1(Long differenceBetweenCurrentAndNewForSupport_type_1) {
        this.differenceBetweenCurrentAndNewForSupport_type_1 = differenceBetweenCurrentAndNewForSupport_type_1;
    }

    public Long getDifferenceBetweenCurrentAndNewForDedicatedEncoder_power_3() {
        return differenceBetweenCurrentAndNewForDedicatedEncoder_power_3;
    }

    public void setDifferenceBetweenCurrentAndNewForDedicatedEncoder_power_3(Long differenceBetweenCurrentAndNewForDedicatedEncoder_power_3) {
        this.differenceBetweenCurrentAndNewForDedicatedEncoder_power_3 = differenceBetweenCurrentAndNewForDedicatedEncoder_power_3;
    }
}
