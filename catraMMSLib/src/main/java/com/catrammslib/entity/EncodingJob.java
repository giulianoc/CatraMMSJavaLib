package com.catrammslib.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by multi on 09.06.18.
 */
public class EncodingJob implements Serializable {
    private Long encodingJobKey;
    private String status;
    private String processorMMS;
    private Long encoderKey;
    private Long encodingPid;
    private Date start;
    private Boolean endEstimate;
    private Date end;
    private Long progress;
    private Long failuresNumber;
    private String encodingPriority;
    private int encodingPriorityCode;
    private int maxEncodingPriorityCode;
    private String type;
    private String parameters;  // the content of this field depend on the 'type' field
    private Long ingestionJobKey;
    private Boolean ownedByCurrentWorkspace;

    // type is 'EncodeVideoAudio' or 'EncodeImage' or 'CutFrameAccurate'
    private Long encodingProfileKey;    // type is also LiveGrid
    private Long sourcePhysicalPathKey;

    // type is 'OverlayImageOnVideo'
    private Long sourceVideoPhysicalPathKey;
    private Long sourceImagePhysicalPathKey;

    // type is 'OverlayTextOnVideo'
    // private Long sourceVideoPhysicalPathKey; already present

    // type is 'FaceRecognition'

    // type is 'LiveRecorder'
    private String liveURL;     // type is also LiveProxy
    private String outputFileFormat;
    private Long segmentDurationInSeconds;  // type is also LiveGrid
    private Date recordingPeriodStart;
    private Date recordingPeriodEnd;

    // type is LiveGrid
    private String inputChannels;


    // type is liveProxy
    private String liveProxyOutputTypes;
    private String liveProxySegmentsDurationInSeconds;
    private Date proxyPeriodStart;
    private Date proxyPeriodEnd;

    // type is 'PictureInPicture'
    private Long mainVideoPhysicalPathKey;
    private Long overlayVideoPhysicalPathKey;

    // type is 'IntroOutroOverlay'
    private Long introVideoPhysicalPathKey;
    // private Long mainVideoPhysicalPathKey;   already present in PictureInPicture
    private Long outroVideoPhysicalPathKey;


    // filled by GUI
    private String mediaTitle;
    private String mediaDuration;


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

    public Long getProgress() {
        return progress;
    }

    public void setProgress(Long progress) {
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
    }

    public Long getEncodingProfileKey() {
        return encodingProfileKey;
    }

    public void setEncodingProfileKey(Long encodingProfileKey) {
        this.encodingProfileKey = encodingProfileKey;
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

    public String getOutputFileFormat() {
        return outputFileFormat;
    }

    public void setOutputFileFormat(String outputFileFormat) {
        this.outputFileFormat = outputFileFormat;
    }

    public Long getSegmentDurationInSeconds() {
        return segmentDurationInSeconds;
    }

    public void setSegmentDurationInSeconds(Long segmentDurationInSeconds) {
        this.segmentDurationInSeconds = segmentDurationInSeconds;
    }

    public Date getRecordingPeriodStart() {
        return recordingPeriodStart;
    }

    public void setRecordingPeriodStart(Date recordingPeriodStart) {
        this.recordingPeriodStart = recordingPeriodStart;
    }

    public Date getRecordingPeriodEnd() {
        return recordingPeriodEnd;
    }

    public void setRecordingPeriodEnd(Date recordingPeriodEnd) {
        this.recordingPeriodEnd = recordingPeriodEnd;
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

    public Long getMainVideoPhysicalPathKey() {
        return mainVideoPhysicalPathKey;
    }

    public void setMainVideoPhysicalPathKey(Long mainVideoPhysicalPathKey) {
        this.mainVideoPhysicalPathKey = mainVideoPhysicalPathKey;
    }

    public Long getOverlayVideoPhysicalPathKey() {
        return overlayVideoPhysicalPathKey;
    }

    public void setOverlayVideoPhysicalPathKey(Long overlayVideoPhysicalPathKey) {
        this.overlayVideoPhysicalPathKey = overlayVideoPhysicalPathKey;
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

    public Boolean getOwnedByCurrentWorkspace() {
        return ownedByCurrentWorkspace;
    }

    public void setOwnedByCurrentWorkspace(Boolean ownedByCurrentWorkspace) {
        this.ownedByCurrentWorkspace = ownedByCurrentWorkspace;
    }

    public Long getIntroVideoPhysicalPathKey() {
        return introVideoPhysicalPathKey;
    }

    public void setIntroVideoPhysicalPathKey(Long introVideoPhysicalPathKey) {
        this.introVideoPhysicalPathKey = introVideoPhysicalPathKey;
    }

    public Long getOutroVideoPhysicalPathKey() {
        return outroVideoPhysicalPathKey;
    }

    public void setOutroVideoPhysicalPathKey(Long outroVideoPhysicalPathKey) {
        this.outroVideoPhysicalPathKey = outroVideoPhysicalPathKey;
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
