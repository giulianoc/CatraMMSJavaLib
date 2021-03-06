package com.catrammslib.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.lang.model.util.ElementScanner6;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by multi on 09.06.18.
 */
public class IngestionJob implements Serializable, Comparable {

    private final Logger mLogger = Logger.getLogger(IngestionJob.class);

	private Long ingestionJobKey;
    private String label;
    private String ingestionType;
    private String metaDataContent;
    private Date processingStartingFrom;
    private Date startProcessing;
    private Boolean endProcessingEstimate;
    private Date endProcessing;
    private String status;
    private String errorMessage;
    private Boolean errorMessageTruncated;
    private String processorMMS;
    private Long downloadingProgress;
    private Long uploadingProgress;
    private List<IngestionJobMediaItem> ingestionJobMediaItemList = new ArrayList<>();
    private EncodingJob encodingJob = null;
    private Long ingestionRootKey;

	private Boolean selected;
    private Date ingestionDate;	// it is not filled by the server, it has to be filled by java client (may be calling getIngestionWorkflow)
	
    private Long dependOnIngestionJobKey;
    private int dependOnSuccess;
    private String dependencyIngestionStatus;

    private String ingester;

    // ingestionType is 'LiveRecorder'
    private String channelLabel;
    private Date recordingPeriodStart;
    private Date recordingPeriodEnd;
    private Boolean recordingVirtualVOD;
    private Boolean recordingMonitorHLS;

    // ingestionType is 'LiveProxy'
    private Date proxyPeriodStart;
    private Date proxyPeriodEnd;

    @Override
    public String toString() {
        return getLabel();
    }

    public boolean isPlayable()
    {
        boolean playable;

        if (getStatus().equalsIgnoreCase("EncodingQueued"))
        {
            if (getIngestionType().equalsIgnoreCase("Live-Grid"))
                playable = true;
            else if (getIngestionType().equalsIgnoreCase("Live-Recorder"))
            {
                if (getRecordingMonitorHLS() || getRecordingVirtualVOD())
                    playable = true;
                else
                    playable = false;
            }
            else if (getIngestionType().equalsIgnoreCase("Live-Proxy")
				|| getIngestionType().equalsIgnoreCase("VOD-Proxy")
				|| getIngestionType().equalsIgnoreCase("Countdown")
			)
            {
				playable = false;

				// it can be played
				// 1. in case we have an HLS as Output
				// 2. in case we have an RTMP_Stream as Output and PlayUrl is filled
				try
				{
					JSONObject joParameters = new JSONObject(metaDataContent);
					if (joParameters.has("Outputs"))
					{
						JSONArray jaOutputs = joParameters.getJSONArray("Outputs");
						for (int outputIndex = 0; outputIndex < jaOutputs.length(); outputIndex++)
						{
							JSONObject joOutput = jaOutputs.getJSONObject(outputIndex);
							if (joOutput.has("OutputType") && joOutput.getString("OutputType").equalsIgnoreCase("HLS"))
							{
								playable = true;

								break;
							}
							else if (joOutput.has("OutputType") && 
								(joOutput.getString("OutputType").equalsIgnoreCase("RTMP_Stream") 
									|| joOutput.getString("OutputType").equalsIgnoreCase("AWS_CHANNEL"))
								&& joOutput.has("PlayUrl") && !joOutput.getString("PlayUrl").isEmpty()
								)
							{
								playable = true;

								break;
							}
						}
					}
				}
				catch(Exception e)
				{
					mLogger.error("Exception: " + e);
				}
            }
            else
                playable = false;
        }
        else
            playable = false;

        return playable;
    }

	public boolean isRunning()
	{
		Date now = new Date();

		if (status != null && status.equalsIgnoreCase("EncodingQueued")
			&& startProcessing != null && startProcessing.getTime() <= now.getTime()
			&& (endProcessing == null 
				|| now.getTime() < endProcessing.getTime())	// in case endProcessingEstimate is true
		)
			return true;
		else
			return false;
	}

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ingestionJobKey == null) ? 0 : ingestionJobKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IngestionJob other = (IngestionJob) obj;
		if (ingestionJobKey == null) {
			if (other.ingestionJobKey != null)
				return false;
		} else if (!ingestionJobKey.equals(other.ingestionJobKey))
			return false;
		return true;
	}

	@Override
	public int compareTo(Object obj) 
	{
		IngestionJob other = (IngestionJob) obj;

		if (recordingPeriodStart != null 
			&& other.getRecordingPeriodStart() != null)
		{
			if (recordingPeriodStart.getTime() < other.getRecordingPeriodStart().getTime())
				return -1;
			else if (recordingPeriodStart.getTime() > other.getRecordingPeriodStart().getTime())
				return 1;
			else
				return 0;
		}
		else if (proxyPeriodStart != null 
			&& other.getProxyPeriodStart() != null)
		{
			if (proxyPeriodStart.getTime() < other.getProxyPeriodStart().getTime())
				return -1;
			else if (proxyPeriodStart.getTime() > other.getProxyPeriodStart().getTime())
				return 1;
			else
				return 0;
		}
		else
			return 0;
	}

	public Date getIngestionDate() {
		return ingestionDate;
	}

	public void setIngestionDate(Date ingestionDate) {
		this.ingestionDate = ingestionDate;
	}

	public String getHtmlErrorMessage() {
        return (errorMessage == null ? errorMessage : errorMessage.replace("\n", "<br/>"));
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getIngestionRootKey() {
        return ingestionRootKey;
    }

    public void setIngestionRootKey(Long ingestionRootKey) {
        this.ingestionRootKey = ingestionRootKey;
    }

    public Long getIngestionJobKey() {
        return ingestionJobKey;
    }

    public void setIngestionJobKey(Long ingestionJobKey) {
        this.ingestionJobKey = ingestionJobKey;
    }

    public Boolean getSelected() {
		return selected;
	}

	public void setSelected(Boolean selected) {
		this.selected = selected;
	}

	public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIngestionType() {
        return ingestionType;
    }

    public void setIngestionType(String ingestionType) {
        this.ingestionType = ingestionType;
    }

    public Date getProcessingStartingFrom() {
        return processingStartingFrom;
    }

    public void setProcessingStartingFrom(Date processingStartingFrom) {
        this.processingStartingFrom = processingStartingFrom;
    }

    public Date getStartProcessing() {
        return startProcessing;
    }

    public void setStartProcessing(Date startProcessing) {
        this.startProcessing = startProcessing;
    }

    public Boolean getEndProcessingEstimate() {
        return endProcessingEstimate;
    }

    public void setEndProcessingEstimate(Boolean endProcessingEstimate) {
        this.endProcessingEstimate = endProcessingEstimate;
    }

    public Date getEndProcessing() {
        return endProcessing;
    }

    public void setEndProcessing(Date endProcessing) {
        this.endProcessing = endProcessing;
    }

    public String getProcessorMMS() {
        return processorMMS;
    }

    public void setProcessorMMS(String processorMMS) {
        this.processorMMS = processorMMS;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIngester() {
        return ingester;
    }

    public void setIngester(String ingester) {
        this.ingester = ingester;
    }

    public Long getDownloadingProgress() {
        return downloadingProgress;
    }

    public void setDownloadingProgress(Long downloadingProgress) {
        this.downloadingProgress = downloadingProgress;
    }

    public Long getUploadingProgress() {
        return uploadingProgress;
    }

    public void setUploadingProgress(Long uploadingProgress) {
        this.uploadingProgress = uploadingProgress;
    }

    public List<IngestionJobMediaItem> getIngestionJobMediaItemList() {
        return ingestionJobMediaItemList;
    }

    public void setIngestionJobMediaItemList(List<IngestionJobMediaItem> ingestionJobMediaItemList) {
        this.ingestionJobMediaItemList = ingestionJobMediaItemList;
    }

    public EncodingJob getEncodingJob() {
        return encodingJob;
    }

    public void setEncodingJob(EncodingJob encodingJob) {
        this.encodingJob = encodingJob;
    }

    public String getMetaDataContent() {
        return metaDataContent;
    }

    public void setMetaDataContent(String metaDataContent) {
        this.metaDataContent = metaDataContent;
    }

    public Long getDependOnIngestionJobKey() {
        return dependOnIngestionJobKey;
    }

    public void setDependOnIngestionJobKey(Long dependOnIngestionJobKey) {
        this.dependOnIngestionJobKey = dependOnIngestionJobKey;
    }

    public String getDependencyIngestionStatus() {
        return dependencyIngestionStatus;
    }

    public void setDependencyIngestionStatus(String dependencyIngestionStatus) {
        this.dependencyIngestionStatus = dependencyIngestionStatus;
    }

    public Boolean getErrorMessageTruncated() {
        return errorMessageTruncated;
    }

    public void setErrorMessageTruncated(Boolean errorMessageTruncated) {
        this.errorMessageTruncated = errorMessageTruncated;
    }

    public int getDependOnSuccess() {
        return dependOnSuccess;
    }

    public void setDependOnSuccess(int dependOnSuccess) {
        this.dependOnSuccess = dependOnSuccess;
    }

    public Date getRecordingPeriodStart() {
        return recordingPeriodStart;
    }

    public void setRecordingPeriodStart(Date recordingPeriodStart) {
        this.recordingPeriodStart = recordingPeriodStart;
    }

    public Date getProxyPeriodStart() {
        return proxyPeriodStart;
    }

    public void setProxyPeriodStart(Date proxyPeriodStart) {
        this.proxyPeriodStart = proxyPeriodStart;
    }

    public Date getProxyPeriodEnd() {
        return proxyPeriodEnd;
    }

    public void setProxyPeriodEnd(Date proxyPeriodEnd) {
        this.proxyPeriodEnd = proxyPeriodEnd;
    }

    public Date getRecordingPeriodEnd() {
        return recordingPeriodEnd;
    }

    public void setRecordingPeriodEnd(Date recordingPeriodEnd) {
        this.recordingPeriodEnd = recordingPeriodEnd;
    }

    public Boolean getRecordingVirtualVOD() {
        return recordingVirtualVOD;
    }

    public void setRecordingVirtualVOD(Boolean recordingVirtualVOD) {
        this.recordingVirtualVOD = recordingVirtualVOD;
    }

    public Boolean getRecordingMonitorHLS() {
        return recordingMonitorHLS;
    }

    public void setRecordingMonitorHLS(Boolean recordingMonitorHLS) {
        this.recordingMonitorHLS = recordingMonitorHLS;
    }

    public String getChannelLabel() {
        return channelLabel;
    }

    public void setChannelLabel(String channelLabel) {
        this.channelLabel = channelLabel;
    }
}
