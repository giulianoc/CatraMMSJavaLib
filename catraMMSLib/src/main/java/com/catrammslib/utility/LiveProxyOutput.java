package com.catrammslib.utility;

import java.io.Serializable;
import org.apache.log4j.Logger;

import org.json.JSONObject;

public class LiveProxyOutput implements Serializable {
    
    private static final Logger mLogger = Logger.getLogger(LiveProxyOutput.class);

	// RTMP_Channel, HLS_Channel, CDN_AWS, CDN_CDN77, HLS, UDP_Stream
	private String outputType;

	// UDP_Stream
    private String udpURL;

	// CDN_AWS
	private String awsChannelConfigurationLabel;	// to be started/stopped
	// CDN_AWS
	private Boolean awsSignedURL;
	// CDN_AWS
	private Long awsExpirationInMinutes;

	// CDN_CDN77
	private String cdn77ChannelConfigurationLabel;
	// CDN_CDN77
	private Long cdn77ExpirationInMinutes;

	// RTMP_Channel
	private String rtmpChannelConfigurationLabel;

	// HLS_Channel
	private String hlsChannelConfigurationLabel;

	private Long videoTrackIndexToBeUsed;
	private Long audioTrackIndexToBeUsed;

	private DrawTextDetails drawTextDetails;

	// HLS
	private String otherOutputOptions;
	// HLS
    private String encodingProfileLabel;
	// HLS
	private JSONObject filters;

	public LiveProxyOutput()
	{
		drawTextDetails = null;
		filters = null;
		videoTrackIndexToBeUsed = (long) -1;
		audioTrackIndexToBeUsed = (long) -1;
	}
	
	public JSONObject toJson()
	{
		JSONObject joOutput = new JSONObject();

		try
		{
			joOutput.put("OutputType", getOutputType());
			if (getOutputType().equalsIgnoreCase("CDN_AWS"))
			{
				if (getAwsChannelConfigurationLabel() != null && !getAwsChannelConfigurationLabel().isEmpty())
					joOutput.put("awsChannelConfigurationLabel", getAwsChannelConfigurationLabel());
				if (getAwsSignedURL() != null)
					joOutput.put("awsSignedURL", getAwsSignedURL());
				if (getAwsExpirationInMinutes() != null)
					joOutput.put("awsExpirationInMinutes", getAwsExpirationInMinutes());
			}
			else if (getOutputType().equalsIgnoreCase("CDN_CDN77"))
			{
				if (getCdn77ChannelConfigurationLabel() != null && !getCdn77ChannelConfigurationLabel().isEmpty())
					joOutput.put("cdn77ChannelConfigurationLabel", getCdn77ChannelConfigurationLabel());
				if (getCdn77ExpirationInMinutes() != null)
					joOutput.put("cdn77ExpirationInMinutes", getCdn77ExpirationInMinutes());
			}
			else if (getOutputType().equalsIgnoreCase("UDP_Stream"))
				joOutput.put("udpUrl", getUdpURL());
			else if (getOutputType().equalsIgnoreCase("RTMP_Channel"))
			{
				if (getRtmpChannelConfigurationLabel() != null && !getRtmpChannelConfigurationLabel().isEmpty())
					joOutput.put("rtmpChannelConfigurationLabel", getRtmpChannelConfigurationLabel());
			}
			else if (getOutputType().equalsIgnoreCase("HLS_Channel"))
			{
				if (getHlsChannelConfigurationLabel() != null && !getHlsChannelConfigurationLabel().isEmpty())
					joOutput.put("hlsChannelConfigurationLabel", getHlsChannelConfigurationLabel());
			}
			else
			{
				mLogger.error("Unknown outputType"
						+ ", outputType: " + getOutputType()
				);
			}
	
			if (getEncodingProfileLabel() != null && !getEncodingProfileLabel().isEmpty())
				joOutput.put("EncodingProfileLabel", getEncodingProfileLabel());
	
			if (getOtherOutputOptions() != null && !getOtherOutputOptions().isEmpty())
				joOutput.put("OtherOutputOptions", getOtherOutputOptions());
	
			if (getVideoTrackIndexToBeUsed() != null && getVideoTrackIndexToBeUsed() != -1)
				joOutput.put("videoTrackIndexToBeUsed",  getVideoTrackIndexToBeUsed());

			if (getAudioTrackIndexToBeUsed() != null && getAudioTrackIndexToBeUsed() != -1)
				joOutput.put("audioTrackIndexToBeUsed",  getAudioTrackIndexToBeUsed());

			if (getFilters() != null)
				joOutput.put("filters", getFilters());
			
			if (drawTextDetails != null)
				joOutput.put("drawTextDetails", drawTextDetails.toJson());
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

	public String getRtmpChannelConfigurationLabel() {
		return rtmpChannelConfigurationLabel;
	}

	public void setRtmpChannelConfigurationLabel(String rtmpChannelConfigurationLabel) {
		this.rtmpChannelConfigurationLabel = rtmpChannelConfigurationLabel;
	}

	public String getUdpURL() {
		return udpURL;
	}

	public void setUdpURL(String udpURL) {
		this.udpURL = udpURL;
	}


	public Long getVideoTrackIndexToBeUsed() {
		return videoTrackIndexToBeUsed;
	}

	public void setVideoTrackIndexToBeUsed(Long videoTrackIndexToBeUsed) {
		this.videoTrackIndexToBeUsed = videoTrackIndexToBeUsed;
	}

	public String getCdn77ChannelConfigurationLabel() {
		return cdn77ChannelConfigurationLabel;
	}

	public void setCdn77ChannelConfigurationLabel(String cdn77ChannelConfigurationLabel) {
		this.cdn77ChannelConfigurationLabel = cdn77ChannelConfigurationLabel;
	}

	public Long getCdn77ExpirationInMinutes() {
		return cdn77ExpirationInMinutes;
	}

	public void setCdn77ExpirationInMinutes(Long cdn77ExpirationInMinutes) {
		this.cdn77ExpirationInMinutes = cdn77ExpirationInMinutes;
	}

	public String getHlsChannelConfigurationLabel() {
		return hlsChannelConfigurationLabel;
	}

	public void setHlsChannelConfigurationLabel(String hlsChannelConfigurationLabel) {
		this.hlsChannelConfigurationLabel = hlsChannelConfigurationLabel;
	}

	public Long getAudioTrackIndexToBeUsed() {
		return audioTrackIndexToBeUsed;
	}

	public void setAudioTrackIndexToBeUsed(Long audioTrackIndexToBeUsed) {
		this.audioTrackIndexToBeUsed = audioTrackIndexToBeUsed;
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

    public String getEncodingProfileLabel() {
        return encodingProfileLabel;
    }

    public void setEncodingProfileLabel(String encodingProfileLabel) {
        this.encodingProfileLabel = encodingProfileLabel;
    }

	public JSONObject getFilters() {
		return filters;
	}

	public void setFilters(JSONObject filters) {
		this.filters = filters;
	}

	public Long getAwsExpirationInMinutes() {
		return awsExpirationInMinutes;
	}

	public void setAwsExpirationInMinutes(Long awsExpirationInMinutes) {
		this.awsExpirationInMinutes = awsExpirationInMinutes;
	}

	public DrawTextDetails getDrawTextDetails() {
		return drawTextDetails;
	}

	public void setDrawTextDetails(DrawTextDetails drawTextDetails) {
		this.drawTextDetails = drawTextDetails;
	}

	public String getOtherOutputOptions() {
        return otherOutputOptions;
    }

    public void setOtherOutputOptions(String otherOutputOptions) {
        this.otherOutputOptions = otherOutputOptions;
    }
}
