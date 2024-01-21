package com.catrammslib.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by multi on 09.06.18.
 */
public class EncodingProfileVideo implements Serializable {
    private String codec;
    private String profile;
    private Boolean twoPasses;
    private String otherOutputParameters;
    private Long frameRate;
    private Long keyFrameIntervalInSeconds;

    private List<VideoBitRate> videoBitRateList = new ArrayList<>();
    // private List<AudioBitRate> audioBitRateList = new ArrayList<>();


    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public Boolean getTwoPasses() {
        return twoPasses;
    }

    public void setTwoPasses(Boolean twoPasses) {
        this.twoPasses = twoPasses;
    }

    public List<VideoBitRate> getVideoBitRateList() {
        return videoBitRateList;
    }

    public void setVideoBitRateList(List<VideoBitRate> videoBitRateList) {
        this.videoBitRateList = videoBitRateList;
    }
    
    public String getOtherOutputParameters() {
        return otherOutputParameters;
    }

    public void setOtherOutputParameters(String otherOutputParameters) {
        this.otherOutputParameters = otherOutputParameters;
    }

    public Long getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(Long frameRate) {
        this.frameRate = frameRate;
    }

    public Long getKeyFrameIntervalInSeconds() {
        return keyFrameIntervalInSeconds;
    }

    public void setKeyFrameIntervalInSeconds(Long keyFrameIntervalInSeconds) {
        this.keyFrameIntervalInSeconds = keyFrameIntervalInSeconds;
    }
}
