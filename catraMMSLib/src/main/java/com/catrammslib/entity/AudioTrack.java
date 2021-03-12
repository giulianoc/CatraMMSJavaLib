package com.catrammslib.entity;

import java.io.Serializable;

/**
 * Created by multi on 08.06.18.
 */
public class AudioTrack implements Serializable{

    private Long audioTrackKey;
    private Long trackIndex;
    private Long durationInMilliSeconds;
    private String codecName;
    private Long bitRate;
    private Long sampleRate;
    private Long channels;
    private String language;


    public Long getAudioTrackKey() {
        return audioTrackKey;
    }

    public void setAudioTrackKey(Long audioTrackKey) {
        this.audioTrackKey = audioTrackKey;
    }

    public Long getDurationInMilliSeconds() {
        return durationInMilliSeconds;
    }

    public void setDurationInMilliSeconds(Long durationInMilliSeconds) {
        this.durationInMilliSeconds = durationInMilliSeconds;
    }

    public String getCodecName() {
        return codecName;
    }

    public void setCodecName(String codecName) {
        this.codecName = codecName;
    }

    public Long getBitRate() {
        return bitRate;
    }

    public void setBitRate(Long bitRate) {
        this.bitRate = bitRate;
    }

    public Long getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(Long sampleRate) {
        this.sampleRate = sampleRate;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Long getTrackIndex() {
        return trackIndex;
    }

    public void setTrackIndex(Long trackIndex) {
        this.trackIndex = trackIndex;
    }

    public Long getChannels() {
        return channels;
    }

    public void setChannels(Long channels) {
        this.channels = channels;
    }
}
