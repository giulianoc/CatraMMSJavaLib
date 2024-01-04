package com.catrammslib.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Created by multi on 08.06.18.
 */
public class VideoTrack implements Serializable{

    private static final Logger mLogger = LoggerFactory.getLogger(VideoTrack.class);

    private Long videoTrackKey;
    private Long trackIndex;
    private Long durationInMilliSeconds;
    private String codecName;
    private Long bitRate;
    private String profile;
    private String avgFrameRate;
    private Float fAvgFrameRate;
    private Long width;
    private Long height;

    public void setAvgFrameRate(String avgFrameRate)
    {
        this.avgFrameRate = avgFrameRate;

        try
        {
            int endIndexOfFrameRate = avgFrameRate.indexOf('/');
            if (endIndexOfFrameRate != -1)
            {
                // la prima parte penso sia il numero di frames
                Float totalDuration = new Float(Long.parseLong(avgFrameRate.substring(0, endIndexOfFrameRate)));
                // la seconda parte penso sia il numero di secondi
                Float totalNumberOfFrames = new Float(Long.parseLong(avgFrameRate.substring(endIndexOfFrameRate + 1)));

                fAvgFrameRate = totalDuration / totalNumberOfFrames;
            }
            else
            {
                throw new Exception("avgFrameRate not well formed"
                        + ", avgFrameRate: " + avgFrameRate
                );
            }
        }
        catch (Exception e)
        {
            mLogger.error("setAvgFrameRate exception: " + e);
        }
    }

    public String getAvgFrameRate()
    {
        return avgFrameRate;
    }

    public Long getDurationInMilliSeconds() {
        return durationInMilliSeconds;
    }

    public void setDurationInMilliSeconds(Long durationInMilliSeconds) {
        this.durationInMilliSeconds = durationInMilliSeconds;
    }

    public Long getBitRate() {
        return bitRate;
    }

    public void setBitRate(Long bitRate) {
        this.bitRate = bitRate;
    }

    public Long getVideoTrackKey() {
        return videoTrackKey;
    }

    public void setVideoTrackKey(Long videoTrackKey) {
        this.videoTrackKey = videoTrackKey;
    }

    public String getCodecName() {
        return codecName;
    }

    public void setCodecName(String codecName) {
        this.codecName = codecName;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public Float getfAvgFrameRate() {
        return fAvgFrameRate;
    }

    public void setfAvgFrameRate(Float fAvgFrameRate) {
        this.fAvgFrameRate = fAvgFrameRate;
    }

    public Long getWidth() {
        return width;
    }

    public void setWidth(Long width) {
        this.width = width;
    }

    public Long getTrackIndex() {
        return trackIndex;
    }

    public void setTrackIndex(Long trackIndex) {
        this.trackIndex = trackIndex;
    }

    public Long getHeight() {
        return height;
    }

    public void setHeight(Long height) {
        this.height = height;
    }
}
