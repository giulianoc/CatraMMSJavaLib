package com.catrammslib.utility;

import java.io.Serializable;
import org.apache.log4j.Logger;

import org.json.JSONObject;

public class LiveProxyOutput implements Serializable {
    
    private static final Logger mLogger = Logger.getLogger(LiveProxyOutput.class);

	// RTMP_Stream, AWS_CHANNEL, HLS, UDP_Stream
	private String outputType;
	// RTMP_Stream
    private String rtmpURL;
	// RTMP_Stream
    private String playURL;
	// UDP_Stream
    private String udpURL;
	// HLS
	private Long deliveryCode;
	// HLS
    private Long segmentDurationInSeconds;

	// AWS_CHANNEL
	private String awsChannelConfigurationLabel;	// to be started/stopped
	// AWS_CHANNEL
	private Boolean awsSignedURL;
	// AWS_CHANNEL
	private Long awsExpirationInMinutes;

	// RTMP_Stream, HLS
	private String otherOutputOptions;
	// RTMP_Stream, HLS
    private String encodingProfileLabel;
	// RTMP_Stream, HLS
	private JSONObject joFilters;
	// RTMP_Stream, HLS
    private Long fadeDuration;

	void LiveProxyOutput()
	{
		joFilters = null;
		fadeDuration = null;
	}
	
	public JSONObject toJson()
	{
		JSONObject joOutput = new JSONObject();

		try
		{
			joOutput.put("OutputType", getOutputType());
			if (getOutputType().equalsIgnoreCase("RTMP_Stream"))
			{
				joOutput.put("RtmpUrl", getRtmpURL());
				if (getPlayURL() != null && !getPlayURL().isEmpty())
					joOutput.put("PlayUrl", getPlayURL());
			}
			else if (getOutputType().equalsIgnoreCase("AWS_CHANNEL"))
			{
				if (getAwsChannelConfigurationLabel() != null && !getAwsChannelConfigurationLabel().isEmpty())
					joOutput.put("awsChannelConfigurationLabel", getAwsChannelConfigurationLabel());
				if (getAwsSignedURL() != null)
					joOutput.put("awsSignedURL", getAwsSignedURL());
				if (getAwsExpirationInMinutes() != null)
					joOutput.put("awsExpirationInMinutes", getAwsExpirationInMinutes());
			}
			else if (getOutputType().equalsIgnoreCase("UDP_Stream"))
				joOutput.put("udpUrl", getUdpURL());
			else
			{
				joOutput.put("DeliveryCode", getDeliveryCode());
				if (getSegmentDurationInSeconds() != null)
					joOutput.put("SegmentDurationInSeconds", getSegmentDurationInSeconds());
			}
	
			if (getEncodingProfileLabel() != null)
				joOutput.put("EncodingProfileLabel", getEncodingProfileLabel());
	
			if (getOtherOutputOptions() != null && !getOtherOutputOptions().isEmpty())
				joOutput.put("OtherOutputOptions", getOtherOutputOptions());
	
			if (getJoFilters() != null)
				joOutput.put("filters", getJoFilters());

			if (getFadeDuration() != null)
				joOutput.put("fadeDuration", getFadeDuration());
		}
		catch(Exception e)
		{
			mLogger.error("Exception: " + e);
		}
		
		return joOutput;
	}

    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public Long getFadeDuration() {
		return fadeDuration;
	}

	public void setFadeDuration(Long fadeDuration) {
		this.fadeDuration = fadeDuration;
	}

	public String getUdpURL() {
		return udpURL;
	}

	public void setUdpURL(String udpURL) {
		this.udpURL = udpURL;
	}


	public String getAwsChannelConfigurationLabel() {
		return awsChannelConfigurationLabel;
	}

	public void setAwsChannelConfigurationLabel(String awsChannelConfigurationLabel) {
		this.awsChannelConfigurationLabel = awsChannelConfigurationLabel;
	}


	public Boolean getAwsSignedURL() {
		return awsSignedURL;
	}

	public void setAwsSignedURL(Boolean awsSignedURL) {
		this.awsSignedURL = awsSignedURL;
	}

	public String getRtmpURL() {
        return rtmpURL;
    }

    public void setRtmpURL(String rtmpURL) {
        this.rtmpURL = rtmpURL;
    }

    public String getPlayURL() {
		return playURL;
	}

	public void setPlayURL(String playURL) {
		this.playURL = playURL;
	}

	public Long getSegmentDurationInSeconds() {
        return segmentDurationInSeconds;
    }

    public void setSegmentDurationInSeconds(Long segmentDurationInSeconds) {
        this.segmentDurationInSeconds = segmentDurationInSeconds;
    }

    public String getEncodingProfileLabel() {
        return encodingProfileLabel;
    }

    public void setEncodingProfileLabel(String encodingProfileLabel) {
        this.encodingProfileLabel = encodingProfileLabel;
    }

    public Long getDeliveryCode() {
        return deliveryCode;
    }

    public void setDeliveryCode(Long deliveryCode) {
        this.deliveryCode = deliveryCode;
    }


    public JSONObject getJoFilters() {
		return joFilters;
	}

	public void setJoFilters(JSONObject joFilters) {
		this.joFilters = joFilters;
	}

	public Long getAwsExpirationInMinutes() {
		return awsExpirationInMinutes;
	}

	public void setAwsExpirationInMinutes(Long awsExpirationInMinutes) {
		this.awsExpirationInMinutes = awsExpirationInMinutes;
	}

	public String getOtherOutputOptions() {
        return otherOutputOptions;
    }

    public void setOtherOutputOptions(String otherOutputOptions) {
        this.otherOutputOptions = otherOutputOptions;
    }
}
