package com.catrammslib.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by multi on 09.06.18.
 */
public class EncodingProfileAudio implements Serializable {
    private String codec;
    private String otherOutputParameters;
    private Long channelsNumber;
    private Long sampleRate;

    private List<AudioBitRate> audioBitRateList = new ArrayList<>();

    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec;
    }

    public String getOtherOutputParameters() {
        return otherOutputParameters;
    }

    public void setOtherOutputParameters(String otherOutputParameters) {
        this.otherOutputParameters = otherOutputParameters;
    }

    public Long getChannelsNumber() {
        return channelsNumber;
    }

    public void setChannelsNumber(Long channelsNumber) {
        this.channelsNumber = channelsNumber;
    }

    public List<AudioBitRate> getAudioBitRateList() {
        return audioBitRateList;
    }

    public void setAudioBitRateList(List<AudioBitRate> audioBitRateList) {
        this.audioBitRateList = audioBitRateList;
    }

    public Long getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(Long sampleRate) {
        this.sampleRate = sampleRate;
    }
}
