package com.catrammslib.entity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// import javax.lang.model.util.ElementScanner6;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by multi on 09.06.18.
 */
public class IngestionJob implements Serializable, Comparable {

    private final Logger mLogger = LoggerFactory.getLogger(IngestionJob.class);

	private Long ingestionJobKey;
    private String label;
    private String ingestionType;
    private String metaDataContent;
    private JSONObject joMetaDataContent;
    private Date processingStartingFrom;
    private Date startProcessing;
    private Boolean endProcessingEstimate;
    private Date endProcessing;
    private String status;
    private JSONArray errorMessages;
    // private Boolean errorMessageTruncated;
    private String processorMMS;
    private Double downloadingProgress;
    private Double uploadingProgress;
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
    // private String recordingPeriodStart_YYYY_MM_DD_HH_MM_SS;
    private Date recordingPeriodEnd;
    // private String recordingPeriodEnd_YYYY_MM_DD_HH_MM_SS;
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
				{
					// it can be played
					// 1. in case we have an HLS as Output
					// 2. in case we have an RTMP_Stream as Output and PlayUrl is filled
					playable = false;
					
					try
					{
						// JSONObject joParameters = new JSONObject(metaDataContent);
						if (joMetaDataContent.has("outputs") || joMetaDataContent.has("Outputs"))
						{
                            JSONArray jaOutputs = joMetaDataContent.optJSONArray("outputs");
                            // if (joMetaDataContent.has("outputs"))
                            //    jaOutputs = joMetaDataContent.getJSONArray("outputs");
                            // else // if (joMetaDataContent.has("Outputs"))
                            //    jaOutputs = joMetaDataContent.getJSONArray("Outputs");
							for (int outputIndex = 0; outputIndex < jaOutputs.length(); outputIndex++)
							{
								JSONObject joOutput = jaOutputs.getJSONObject(outputIndex);
								if (joOutput.has("outputType") && joOutput.getString("outputType").equalsIgnoreCase("HLS"))
								{
									playable = true;
	
									break;
								}
								else if (joOutput.has("outputType") &&
									(joOutput.getString("outputType").equalsIgnoreCase("RTMP_Stream")
                                        || joOutput.getString("outputType").equalsIgnoreCase("RTMP_Channel")
                                    )
                                    // la playURL è stata eliminata perchè le url temporizzate scadevano, non era possibile,
                                    // mantenere sempre la stessa. Ora viene calcolata in tempo reale
									// && ((joOutput.has("PlayUrl") && !joOutput.getString("PlayUrl").isEmpty())
                                    //    || (joOutput.has("playUrl") && !joOutput.getString("playUrl").isEmpty()))
									)
								{
                                    // assumiamo che in questo scenario sia sempre playable
									playable = true;
	
									break;
								}
                                /*
                                else if (joOutput.has("OutputType") && joOutput.getString("OutputType").equalsIgnoreCase("HLS"))
                                {
                                    playable = true;

                                    break;
                                }
                                else if (joOutput.has("OutputType") &&
                                        (joOutput.getString("OutputType").equalsIgnoreCase("RTMP_Stream")
                                                || joOutput.getString("OutputType").equalsIgnoreCase("RTMP_Channel")
                                        )
                                        && ((joOutput.has("PlayUrl") && !joOutput.getString("PlayUrl").isEmpty())
                                            || (joOutput.has("playUrl") && !joOutput.getString("playUrl").isEmpty()))
                                )
                                {
                                    playable = true;

                                    break;
                                }
                                 */
							}
						}
					}
					catch(Exception e)
					{
						mLogger.error("Exception: " + e);
					}
				}
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
					// JSONObject joParameters = new JSONObject(metaDataContent);
					if (joMetaDataContent.has("outputs") || joMetaDataContent.has("Outputs"))
					{
                        JSONArray jaOutputs = joMetaDataContent.optJSONArray("outputs");
//                        if (joMetaDataContent.has("outputs"))
//						    jaOutputs = joMetaDataContent.getJSONArray("outputs");
//                        else // if (joMetaDataContent.has("Outputs"))
//                            jaOutputs = joMetaDataContent.getJSONArray("Outputs");
						for (int outputIndex = 0; outputIndex < jaOutputs.length(); outputIndex++)
						{
							JSONObject joOutput = jaOutputs.getJSONObject(outputIndex);
							if (joOutput.has("outputType")
                                    && joOutput.getString("outputType").equalsIgnoreCase("HLS_Channel"))
							{
								playable = true;

								break;
							}
							else if (joOutput.has("outputType") &&
								joOutput.getString("outputType").equalsIgnoreCase("RTMP_Channel")
                                    // la playURL è stata eliminata perchè le url temporizzate scadevano, non era possibile,
                                    // mantenere sempre la stessa. Ora viene calcolata in tempo reale
//                                && ((joOutput.has("PlayUrl") && !joOutput.getString("PlayUrl").isEmpty())
//                                    || (joOutput.has("playUrl") && !joOutput.getString("playUrl").isEmpty()))
								)
							{
                                // assumiamo che in questo scenario sia sempre playable
								playable = true;

								break;
							}
                            /*
                            else if (joOutput.has("OutputType")
                                    && joOutput.getString("OutputType").equalsIgnoreCase("HLS_Channel"))
                            {
                                playable = true;

                                break;
                            }
                            else if (joOutput.has("OutputType") &&
                                    (joOutput.getString("OutputType").equalsIgnoreCase("RTMP_Channel")
                                    )
                                    && ((joOutput.has("PlayUrl") && !joOutput.getString("PlayUrl").isEmpty())
                                        || (joOutput.has("playUrl") && !joOutput.getString("playUrl").isEmpty()))
                            )
                            {
                                playable = true;

                                break;
                            }
                             */
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
		long now = System.currentTimeMillis();

		if (status != null && status.equalsIgnoreCase("EncodingQueued")
			&& startProcessing != null && startProcessing.getTime() <= now
			&& (endProcessing == null 
				|| now < endProcessing.getTime())	// in case endProcessingEstimate is true
		)
			return true;
		else
			return false;
	}

    public boolean isPlanned() {
        long now = System.currentTimeMillis();

        if (ingestionType.equalsIgnoreCase("Live-Recorder"))
        {
            if (status != null
                    && (status.equalsIgnoreCase("EncodingQueued") || status.equalsIgnoreCase("Start_TaskQueued"))
                    && recordingPeriodStart != null && now <= recordingPeriodStart.getTime()
            )
                return true;
            else
                return false;
        }
        else
        {
            if (status != null
                    && (status.equalsIgnoreCase("EncodingQueued") || status.equalsIgnoreCase("Start_TaskQueued"))
                    && proxyPeriodStart != null && now <= proxyPeriodStart.getTime()
            )
                return true;
            else
                return false;
        }
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
		if (obj == null || getClass() != obj.getClass())
            return false;
		IngestionJob other = (IngestionJob) obj;
		if ((ingestionJobKey == null && other.ingestionJobKey != null)
            || (ingestionJobKey != null && other.ingestionJobKey == null)
        )
            return false;
		else if (ingestionJobKey.longValue() != other.ingestionJobKey.longValue())
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

    public JSONObject getJoMetaDataContent() {
        return joMetaDataContent;
    }

    public void setJoMetaDataContent(JSONObject joMetaDataContent) {
        this.joMetaDataContent = joMetaDataContent;
    }

    public void setMetaDataContent(String metaDataContent) {

        this.metaDataContent = metaDataContent;

        if (metaDataContent != null && !metaDataContent.isEmpty())
        {
            try
            {
                joMetaDataContent = new JSONObject(metaDataContent);
            }
            catch (Exception e)
            {
                mLogger.error("Exception"
                        + ", exception: " + e
                        + ", metaDataContent: " + metaDataContent
                );
            }
        }
    }

    public String getErrorMessagesAsHTML() {
        if (errorMessages == null)
            return null;

        StringBuilder sb = new StringBuilder(1024);
        try {
            sb.append("<ol>");
            for(int index = 0; index < errorMessages.length(); index++)
                sb.append("<li>" + errorMessages.getString(index) + "</li>");
            sb.append("</ol>");
        }
        catch (Exception e)
        {
            mLogger.error("Exception: " + e);
        }

        return sb.toString();
    }

    public JSONArray getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(JSONArray errorMessages) {
        this.errorMessages = errorMessages;
    }

    public Date getIngestionDate() {
		return ingestionDate;
	}

	public void setIngestionDate(Date ingestionDate) {
		this.ingestionDate = ingestionDate;
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


    public Double getDownloadingProgress() {
        return downloadingProgress;
    }

    public void setDownloadingProgress(Double downloadingProgress) {
        this.downloadingProgress = downloadingProgress;
    }

    public Double getUploadingProgress() {
        return uploadingProgress;
    }

    public void setUploadingProgress(Double uploadingProgress) {
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
        /*
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            recordingPeriodStart_YYYY_MM_DD_HH_MM_SS = simpleDateFormat.format(recordingPeriodStart);
        }
        catch (Exception e)
        {
            mLogger.error("exception: " + e);
        }
         */
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
        /*
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            recordingPeriodEnd_YYYY_MM_DD_HH_MM_SS = simpleDateFormat.format(recordingPeriodEnd);
        }
        catch (Exception e)
        {
            mLogger.error("exception: " + e);
        }
         */
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
