package com.catrammslib.entity;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by multi on 09.06.18.
 */
public class EncodingJob implements Serializable {
    private final Logger mLogger = LoggerFactory.getLogger(this.getClass());
    private Long encodingJobKey;
    private String status;
    private String processorMMS;
    private Long encoderKey;
    private Long encodingPid;
    private JSONObject realTimeInfo;
    private Long numberOfRestartBecauseOfFailure;
    private Date start;
    private Boolean endEstimate;
    private Date end;
    private Double progress;
    private Long failuresNumber;
    private String encodingPriority;
    private int encodingPriorityCode;
    private int maxEncodingPriorityCode;
    private String type;
    private String parameters;  // the content of this field depend on the 'type' field
    private JSONObject joParameters;
    private Long ingestionJobKey;
    private Boolean ownedByCurrentWorkspace;

    // type is 'EncodeVideoAudio' or 'EncodeImage' or 'CutFrameAccurate'
    private Long encodingProfileKey;    // type is also LiveGrid
    private Long sourcePhysicalPathKey;	// type is also videoSpeed

    // type is 'OverlayImageOnVideo'
    private Long sourceVideoPhysicalPathKey;
    private Long sourceImagePhysicalPathKey;

    // type is 'OverlayTextOnVideo'

    // type is 'FaceRecognition'

    // type is 'LiveRecorder'
    private String liveURL;     // type is also LiveProxy

    // type is LiveGrid
    private String inputChannels;


    // type is liveProxy
    private String liveProxyOutputTypes;
    private String liveProxySegmentsDurationInSeconds;
    private Date proxyPeriodStart;
    private Date proxyPeriodEnd;

    // type is 'PictureInPicture'
    private Long mainSourcePhysicalPathKey;		// type is also IntroOutroOverlay
    private Long overlaySourcePhysicalPathKey;

    // type is 'IntroOutroOverlay'
    private Long introSourcePhysicalPathKey;
    private Long outroSourcePhysicalPathKey;


    // filled by GUI
    private String mediaTitle;
    private String mediaDuration;


    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((encodingJobKey == null) ? 0 : encodingJobKey.hashCode());
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
		EncodingJob other = (EncodingJob) obj;
		if (encodingJobKey == null) {
			if (other.encodingJobKey != null)
				return false;
		} else if (!encodingJobKey.equals(other.encodingJobKey))
			return false;
		return true;
	}

    public String getRealTimeInfoAsHTML()
    {
        try {
            if (realTimeInfo == null)
                return null;

            StringBuilder sb = new StringBuilder();
            sb.append("<ul>");
            Iterator<String> keys = realTimeInfo.keys();
            while (keys.hasNext())
            {
                String key = keys.next();
                Object value = realTimeInfo.get(key);

                String valueStr;
                if (value instanceof String) {
                    valueStr = (String) value;
                } else if (value instanceof Number || value instanceof Boolean || value instanceof Double) {
                    valueStr = value.toString();
                } else {
                    // JSONObject, JSONArray, ecc.
                    valueStr = value.toString();
                }

                sb.append("<li>").append(key).append(": ").append(valueStr).append("</li>");
            }
            sb.append("</ul>");

            return sb.toString();
        }
        catch (Exception e)
        {
            mLogger.error("exception: " + e);
            return null;
        }
    }

	public Long getEncodingJobKey() {
        return encodingJobKey;
    }

    public void setEncodingJobKey(Long encodingJobKey) {
        this.encodingJobKey = encodingJobKey;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public Double getProgress() {
        return progress;
    }

    public void setProgress(Double progress) {
        this.progress = progress;
    }

    public Long getFailuresNumber() {
        return failuresNumber;
    }

    public void setFailuresNumber(Long failuresNumber) {
        this.failuresNumber = failuresNumber;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {

        this.parameters = parameters;

        if (parameters != null && !parameters.isEmpty())
        {
            try
            {
                joParameters = new JSONObject(parameters);
            }
            catch (Exception e)
            {
                /*
                mLogger.error("Exception"
                        + ", exception: " + e
                        + ", parameters: " + parameters
                );
                 */
            }
        }
    }

    public Long getEncodingProfileKey() {
        return encodingProfileKey;
    }

    public void setEncodingProfileKey(Long encodingProfileKey) {
        this.encodingProfileKey = encodingProfileKey;
    }

    public JSONObject getJoParameters() {
        return joParameters;
    }

    public void setJoParameters(JSONObject joParameters) {
        this.joParameters = joParameters;
    }

    public Long getSourcePhysicalPathKey() {
        return sourcePhysicalPathKey;
    }

    public void setSourcePhysicalPathKey(Long sourcePhysicalPathKey) {
        this.sourcePhysicalPathKey = sourcePhysicalPathKey;
    }

    public Long getSourceVideoPhysicalPathKey() {
        return sourceVideoPhysicalPathKey;
    }

    public void setSourceVideoPhysicalPathKey(Long sourceVideoPhysicalPathKey) {
        this.sourceVideoPhysicalPathKey = sourceVideoPhysicalPathKey;
    }

    public Long getSourceImagePhysicalPathKey() {
        return sourceImagePhysicalPathKey;
    }

    public void setSourceImagePhysicalPathKey(Long sourceImagePhysicalPathKey) {
        this.sourceImagePhysicalPathKey = sourceImagePhysicalPathKey;
    }

    public String getEncodingPriority() {
        return encodingPriority;
    }

    public void setEncodingPriority(String encodingPriority) {
        this.encodingPriority = encodingPriority;
    }

    public int getEncodingPriorityCode() {
        return encodingPriorityCode;
    }

    public void setEncodingPriorityCode(int encodingPriorityCode) {
        this.encodingPriorityCode = encodingPriorityCode;
    }

    public int getMaxEncodingPriorityCode() {
        return maxEncodingPriorityCode;
    }

    public void setMaxEncodingPriorityCode(int maxEncodingPriorityCode) {
        this.maxEncodingPriorityCode = maxEncodingPriorityCode;
    }

    public String getLiveURL() {
        return liveURL;
    }

    public void setLiveURL(String liveURL) {
        this.liveURL = liveURL;
    }

    public Boolean getEndEstimate() {
        return endEstimate;
    }

    public void setEndEstimate(Boolean endEstimate) {
        this.endEstimate = endEstimate;
    }

    public String getProcessorMMS() {
        return processorMMS;
    }

    public void setProcessorMMS(String processorMMS) {
        this.processorMMS = processorMMS;
    }

    public Long getMainSourcePhysicalPathKey() {
		return mainSourcePhysicalPathKey;
	}

	public void setMainSourcePhysicalPathKey(Long mainSourcePhysicalPathKey) {
		this.mainSourcePhysicalPathKey = mainSourcePhysicalPathKey;
	}

	public Long getOverlaySourcePhysicalPathKey() {
		return overlaySourcePhysicalPathKey;
	}

	public void setOverlaySourcePhysicalPathKey(Long overlaySourcePhysicalPathKey) {
		this.overlaySourcePhysicalPathKey = overlaySourcePhysicalPathKey;
	}

    public String getLiveProxyOutputTypes() {
        return liveProxyOutputTypes;
    }

    public void setLiveProxyOutputTypes(String liveProxyOutputTypes) {
        this.liveProxyOutputTypes = liveProxyOutputTypes;
    }

    public String getLiveProxySegmentsDurationInSeconds() {
        return liveProxySegmentsDurationInSeconds;
    }

    public void setLiveProxySegmentsDurationInSeconds(String liveProxySegmentsDurationInSeconds) {
        this.liveProxySegmentsDurationInSeconds = liveProxySegmentsDurationInSeconds;
    }

    public String getInputChannels() {
        return inputChannels;
    }

    public void setInputChannels(String inputChannels) {
        this.inputChannels = inputChannels;
    }

    public Long getEncoderKey() {
        return encoderKey;
    }

    public void setEncoderKey(Long encoderKey) {
        this.encoderKey = encoderKey;
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

    public Long getEncodingPid() {
        return encodingPid;
    }

    public void setEncodingPid(Long encodingPid) {
        this.encodingPid = encodingPid;
    }

    public JSONObject getRealTimeInfo() {
        return realTimeInfo;
    }

    public void setRealTimeInfo(JSONObject realTimeInfo) {
        this.realTimeInfo = realTimeInfo;
    }

    public Long getNumberOfRestartBecauseOfFailure() {
        return numberOfRestartBecauseOfFailure;
    }

    public void setNumberOfRestartBecauseOfFailure(Long numberOfRestartBecauseOfFailure) {
        this.numberOfRestartBecauseOfFailure = numberOfRestartBecauseOfFailure;
    }

    public Boolean getOwnedByCurrentWorkspace() {
        return ownedByCurrentWorkspace;
    }

    public void setOwnedByCurrentWorkspace(Boolean ownedByCurrentWorkspace) {
        this.ownedByCurrentWorkspace = ownedByCurrentWorkspace;
    }

    public Long getIntroSourcePhysicalPathKey() {
		return introSourcePhysicalPathKey;
	}

	public void setIntroSourcePhysicalPathKey(Long introSourcePhysicalPathKey) {
		this.introSourcePhysicalPathKey = introSourcePhysicalPathKey;
	}

	public Long getOutroSourcePhysicalPathKey() {
		return outroSourcePhysicalPathKey;
	}

	public void setOutroSourcePhysicalPathKey(Long outroSourcePhysicalPathKey) {
		this.outroSourcePhysicalPathKey = outroSourcePhysicalPathKey;
	}

	public String getMediaTitle() {
        return mediaTitle;
    }

    public void setMediaTitle(String mediaTitle) {
        this.mediaTitle = mediaTitle;
    }

    public String getMediaDuration() {
        return mediaDuration;
    }

    public void setMediaDuration(String mediaDuration) {
        this.mediaDuration = mediaDuration;
    }

    public Long getIngestionJobKey() {
        return ingestionJobKey;
    }

    public void setIngestionJobKey(Long ingestionJobKey) {
        this.ingestionJobKey = ingestionJobKey;
    }
}
