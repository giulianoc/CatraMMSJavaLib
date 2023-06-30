package com.catrammslib.helper.entity;

import com.catrammslib.entity.IngestionJob;
import com.catrammslib.entity.Stream;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Objects;

public class LiveStreamInfo implements Serializable {

    private Stream stream;
    private boolean restreamStaging;    // ciborTV
    private boolean restreamProduction; // ciborTV

    private String liveGridChannelStatus;
    private String liveProxyChannelStatus;
    private String liveRecorderChannelStatus;
    private String lastChannelStatus;   // channelStatus of the last IngestionJob
    private String htmlChannelStatus;   // channel status of all the IngestionJob (Grid, Proxy, Recorder)

    private IngestionJob liveGridIngestionJob;
    private IngestionJob liveProxyIngestionJob;
    private IngestionJob liveRecorderIngestionJob;
    private IngestionJob lastIngestionJob;

    private String liveGridErrorInfo;
    private String liveProxyErrorInfo;
    private String liveRecorderErrorInfo;
    private String lastErrorInfo;

    private boolean playable;


    public static String liveGridStreamingWithoutEncodingJob = "Grid: no EncodingJob allocated yet";
    public static String liveGridStreamingWithoutTranscoderYet = "Grid: no Transcoder allocated yet";
    public static String liveGridStreamingNotWorking = "Grid: streaming not working";
    public static String liveGridStreamingRunning = "Grid: running";
    public static String LiveGridWorkflowNotRunning = "Grid: workflow not running";
    public static String liveGridStreamingStoppedyUser = "Grid: streaming stopped by user";
    public static String liveGridStreamingStoppedyMMS = "Grid: streaming stopped by MMS";
    public static String liveProxyStreamingWithoutEncodingJob = "Proxy: no EncodingJob allocated yet";
    public static String liveProxyStreamingWithoutTranscoderYet = "Proxy: no Transcoder allocated yet";
    public static String liveProxyStreamingNotWorking = "Proxy: streaming not working";
    public static String liveProxyStreamingRunning = "Proxy: running";
    public static String liveProxyStreamingRunningButSomeRequestsFailed = "Proxy: running but we had some failure";
    public static String liveProxyStreamingStoppedyUser = "Proxy: streaming stopped by user";
    public static String liveProxyStreamingStoppedyMMS = "Proxy: streaming stopped by MMS";
    public static String LiveProxyWorkflowNotRunning = "Proxy: workflow not running";
    public static String liveRecorderStreamingWithoutTranscoderYet = "Recorder: no Transcoder allocated yet";
    public static String liveRecorderFinishedOK = "Recorder: finished successful";
    public static String liveRecorderRunning = "Recorder: running";
    public static String liveRecorderFailed = "Recorder: failed";
    public static String liveRecorderWithoutEncodingJob = "Recorder: no EncodingJob allocated yet";
    public static String LiveRecorderWorkflowNotRunning = "Recorder: workflow not running";


    public LiveStreamInfo(Stream stream)
    {
        this.stream = stream;

        if (stream.getUserData() != null && !stream.getUserData().isEmpty())
        {
            try {
                JSONObject joLiveURLData = new JSONObject(stream.getUserData());
                if (joLiveURLData.has("staging") && !joLiveURLData.isNull("staging"))
                    restreamStaging = joLiveURLData.getBoolean("staging");
                if (joLiveURLData.has("production") && !joLiveURLData.isNull("production"))
                    restreamProduction = joLiveURLData.getBoolean("production");
            }
            catch (Exception e)
            {
                /*
                mLogger.error("Restream, exception setting editRestream..."
                        + ", exception: " + e
                );
                */
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LiveStreamInfo that = (LiveStreamInfo) o;
        return Objects.equals(stream, that.stream);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stream);
    }

    public String getStatusStyleColor(String status)
    {
        String styleColor = "";

        if (status != null)
        {
            if (status.equalsIgnoreCase(liveGridStreamingRunning)
                    || status.equalsIgnoreCase(liveProxyStreamingRunning)
                    || status.equalsIgnoreCase(liveGridStreamingStoppedyUser)
                    || status.equalsIgnoreCase(liveProxyStreamingStoppedyUser)
                    || status.startsWith(liveRecorderFinishedOK)
                    || status.equalsIgnoreCase(liveRecorderRunning)
                    || status.equalsIgnoreCase(liveGridStreamingWithoutTranscoderYet)
                    || status.equalsIgnoreCase(liveProxyStreamingWithoutTranscoderYet)
                    || status.equalsIgnoreCase(liveRecorderStreamingWithoutTranscoderYet)
            )
                styleColor = "#28a745"; //"successFullColor";
            else if (status.equalsIgnoreCase(liveGridStreamingNotWorking)
                    || status.equalsIgnoreCase(liveGridStreamingStoppedyMMS)
                    || status.equalsIgnoreCase(liveProxyStreamingStoppedyMMS)
                    || status.equalsIgnoreCase(liveProxyStreamingNotWorking)
                    || status.equalsIgnoreCase(liveRecorderFailed)
            )
                styleColor = "#dc3545"; // "failureColor";
            else if (status.equalsIgnoreCase(liveGridStreamingWithoutEncodingJob)
                    || status.equalsIgnoreCase(liveProxyStreamingWithoutEncodingJob)
                    || status.equalsIgnoreCase(liveRecorderWithoutEncodingJob)
                    || status.equalsIgnoreCase(LiveGridWorkflowNotRunning)
                    || status.equalsIgnoreCase(LiveProxyWorkflowNotRunning)
                    || status.equalsIgnoreCase(LiveRecorderWorkflowNotRunning)
                    || status.equalsIgnoreCase(liveProxyStreamingRunningButSomeRequestsFailed)
            )
                styleColor = "#ffa500"; // "warningColor";
        }

        return styleColor;
    }

    public String getHtmlChannelStatus() {
        return htmlChannelStatus;
    }

    public void setHtmlChannelStatus(String htmlChannelStatus) {
        this.htmlChannelStatus = htmlChannelStatus;
    }

    public Stream getStream() {
		return stream;
	}

	public void setStream(Stream stream) {
		this.stream = stream;
	}

	public boolean isRestreamStaging() {
        return restreamStaging;
    }

    public void setRestreamStaging(boolean restreamStaging) {
        this.restreamStaging = restreamStaging;
    }

    public boolean isRestreamProduction() {
        return restreamProduction;
    }

    public void setRestreamProduction(boolean restreamProduction) {
        this.restreamProduction = restreamProduction;
    }

    public String getLiveGridChannelStatus() {
        return liveGridChannelStatus;
    }

    public void setLiveGridChannelStatus(String liveGridChannelStatus) {
        this.liveGridChannelStatus = liveGridChannelStatus;
    }

    public String getLiveProxyChannelStatus() {
        return liveProxyChannelStatus;
    }

    public void setLiveProxyChannelStatus(String liveProxyChannelStatus) {
        this.liveProxyChannelStatus = liveProxyChannelStatus;
    }

    public String getLiveRecorderChannelStatus() {
        return liveRecorderChannelStatus;
    }

    public void setLiveRecorderChannelStatus(String liveRecorderChannelStatus) {
        this.liveRecorderChannelStatus = liveRecorderChannelStatus;
    }

    public String getLastChannelStatus() {
        return lastChannelStatus;
    }

    public void setLastChannelStatus(String lastChannelStatus) {
        this.lastChannelStatus = lastChannelStatus;
    }

    public String getLiveGridErrorInfo() {
        return liveGridErrorInfo;
    }

    public void setLiveGridErrorInfo(String liveGridErrorInfo) {
        this.liveGridErrorInfo = liveGridErrorInfo;
    }

    public String getLiveProxyErrorInfo() {
        return liveProxyErrorInfo;
    }

    public void setLiveProxyErrorInfo(String liveProxyErrorInfo) {
        this.liveProxyErrorInfo = liveProxyErrorInfo;
    }

    public String getLiveRecorderErrorInfo() {
        return liveRecorderErrorInfo;
    }

    public void setLiveRecorderErrorInfo(String liveRecorderErrorInfo) {
        this.liveRecorderErrorInfo = liveRecorderErrorInfo;
    }

    public String getLastErrorInfo() {
        return lastErrorInfo;
    }

    public void setLastErrorInfo(String lastErrorInfo) {
        this.lastErrorInfo = lastErrorInfo;
    }

    public IngestionJob getLiveGridIngestionJob() {
        return liveGridIngestionJob;
    }

    public void setLiveGridIngestionJob(IngestionJob liveGridIngestionJob) {
        this.liveGridIngestionJob = liveGridIngestionJob;
    }

    public IngestionJob getLiveProxyIngestionJob() {
        return liveProxyIngestionJob;
    }

    public void setLiveProxyIngestionJob(IngestionJob liveProxyIngestionJob) {
        this.liveProxyIngestionJob = liveProxyIngestionJob;
    }

    public IngestionJob getLiveRecorderIngestionJob() {
        return liveRecorderIngestionJob;
    }

    public void setLiveRecorderIngestionJob(IngestionJob liveRecorderIngestionJob) {
        this.liveRecorderIngestionJob = liveRecorderIngestionJob;
    }

    public IngestionJob getLastIngestionJob() {
        return lastIngestionJob;
    }

    public void setLastIngestionJob(IngestionJob lastIngestionJob) {
        this.lastIngestionJob = lastIngestionJob;
    }

    public boolean isPlayable() {
        return playable;
    }

    public void setPlayable(boolean playable) {
        this.playable = playable;
    }
}
