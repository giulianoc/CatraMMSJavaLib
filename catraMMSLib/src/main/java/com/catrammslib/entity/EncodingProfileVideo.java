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
    private Long width;
    private Long height;
    private Boolean twoPasses;
    private Long kBitRate;
    private String otherOutputParameters;
    private Long kMaxRate;
    private Long kBufSize;
    private Long frameRate;
    private Long keyFrameIntervalInSeconds;

    private List<VideoBitRate> videoBitRateList = new ArrayList<>();
    private List<AudioBitRate> audioBitRateList = new ArrayList<>();


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

    public Long getWidth() {
        return width;
    }

    public void setWidth(Long width) {
        this.width = width;
    }

    public Long getHeight() {
        return height;
    }

    public void setHeight(Long height) {
        this.height = height;
    }

    public Boolean getTwoPasses() {
        return twoPasses;
    }

    public void setTwoPasses(Boolean twoPasses) {
        this.twoPasses = twoPasses;
    }

    public Long getkBitRate() {
        return kBitRate;
    }

    public void setkBitRate(Long kBitRate) {
        this.kBitRate = kBitRate;
    }

    public List<VideoBitRate> getVideoBitRateList() {
        return videoBitRateList;
    }

    public void setVideoBitRateList(List<VideoBitRate> videoBitRateList) {
        this.videoBitRateList = videoBitRateList;
    }

    public List<AudioBitRate> getAudioBitRateList() {
        return audioBitRateList;
    }

    public void setAudioBitRateList(List<AudioBitRate> audioBitRateList) {
        this.audioBitRateList = audioBitRateList;
    }

    public String getOtherOutputParameters() {
        return otherOutputParameters;
    }

    public void setOtherOutputParameters(String otherOutputParameters) {
        this.otherOutputParameters = otherOutputParameters;
    }

    public Long getkMaxRate() {
        return kMaxRate;
    }

    public void setkMaxRate(Long kMaxRate) {
        this.kMaxRate = kMaxRate;
    }

    public Long getkBufSize() {
        return kBufSize;
    }

    public void setkBufSize(Long kBufSize) {
        this.kBufSize = kBufSize;
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
