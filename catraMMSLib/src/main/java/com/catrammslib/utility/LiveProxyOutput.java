package com.catrammslib.utility;

import java.io.Serializable;

public class LiveProxyOutput implements Serializable {
    
	// RTMP_Stream, HLS, UDP_Stream
	private String outputType;
	// RTMP_Stream
    private String rtmpURL;
	// UDP_Stream
    private String udpURL;
	// HLS
	private Long deliveryCode;
	// HLS
    private Long segmentDurationInSeconds;

	// RTMP_Stream, HLS
	private String otherOutputOptions;
	// RTMP_Stream, HLS
    private String encodingProfileLabel;
	// RTMP_Stream, HLS
    private String audioVolumeChange;

	
    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public String getUdpURL() {
		return udpURL;
	}

	public void setUdpURL(String udpURL) {
		this.udpURL = udpURL;
	}

	public String getRtmpURL() {
        return rtmpURL;
    }

    public void setRtmpURL(String rtmpURL) {
        this.rtmpURL = rtmpURL;
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

    public String getAudioVolumeChange() {
        return audioVolumeChange;
    }

    public void setAudioVolumeChange(String audioVolumeChange) {
        this.audioVolumeChange = audioVolumeChange;
    }

    public String getOtherOutputOptions() {
        return otherOutputOptions;
    }

    public void setOtherOutputOptions(String otherOutputOptions) {
        this.otherOutputOptions = otherOutputOptions;
    }
}
