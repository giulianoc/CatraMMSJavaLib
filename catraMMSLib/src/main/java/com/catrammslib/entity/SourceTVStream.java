package com.catrammslib.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by multi on 08.06.18.
 */
public class SourceTVStream implements Serializable{

    private Long confKey;
    private Long serviceId;
    private Long networkId;
    private Long transportStreamId;
    private String name;
    private String satellite;
    private Long frequency;
    private String lnb;
    private Long videoPid;
    private String audioPids;
    private Long audioItalianPid;
    private Long audioEnglishPid;
    private Long teletextPid;
    private String modulation;
    private String polarization;
    private Long symbolRate;
    private String country;
    private String deliverySystem;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceTVStream that = (SourceTVStream) o;
        return Objects.equals(confKey, that.confKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceId);
    }

    public Long getConfKey() {
        return confKey;
    }

    public void setConfKey(Long confKey) {
        this.confKey = confKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Long getNetworkId() {
        return networkId;
    }

    public void setNetworkId(Long networkId) {
        this.networkId = networkId;
    }

    public Long getTransportStreamId() {
        return transportStreamId;
    }

    public void setTransportStreamId(Long transportStreamId) {
        this.transportStreamId = transportStreamId;
    }

    public String getSatellite() {
        return satellite;
    }

    public void setSatellite(String satellite) {
        this.satellite = satellite;
    }

    public Long getFrequency() {
        return frequency;
    }

    public void setFrequency(Long frequency) {
        this.frequency = frequency;
    }

    public String getLnb() {
        return lnb;
    }

    public void setLnb(String lnb) {
        this.lnb = lnb;
    }

    public Long getVideoPid() {
        return videoPid;
    }

    public void setVideoPid(Long videoPid) {
        this.videoPid = videoPid;
    }

    public String getAudioPids() {
        return audioPids;
    }

    public void setAudioPids(String audioPids) {
        this.audioPids = audioPids;
    }

    public Long getAudioItalianPid() {
        return audioItalianPid;
    }

    public void setAudioItalianPid(Long audioItalianPid) {
        this.audioItalianPid = audioItalianPid;
    }

    public Long getAudioEnglishPid() {
        return audioEnglishPid;
    }

    public void setAudioEnglishPid(Long audioEnglishPid) {
        this.audioEnglishPid = audioEnglishPid;
    }

    public Long getTeletextPid() {
        return teletextPid;
    }

    public void setTeletextPid(Long teletextPid) {
        this.teletextPid = teletextPid;
    }

    public String getModulation() {
        return modulation;
    }

    public void setModulation(String modulation) {
        this.modulation = modulation;
    }

    public String getPolarization() {
        return polarization;
    }

    public void setPolarization(String polarization) {
        this.polarization = polarization;
    }

    public Long getSymbolRate() {
        return symbolRate;
    }

    public void setSymbolRate(Long symbolRate) {
        this.symbolRate = symbolRate;
    }

    public String getDeliverySystem() {
        return deliverySystem;
    }

    public void setDeliverySystem(String deliverySystem) {
        this.deliverySystem = deliverySystem;
    }
}
