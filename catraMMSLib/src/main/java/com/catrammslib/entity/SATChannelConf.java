package com.catrammslib.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by multi on 08.06.18.
 */
public class SATChannelConf implements Serializable{

    private Long confKey;
    private Long sourceSATConfKey;
    private String name;
    private String region;
    private String country;
    private Long imageMediaItemKey;
    private String imageUniqueName;
    private Long position;
    private String channelData;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SATChannelConf that = (SATChannelConf) o;
        return Objects.equals(confKey, that.confKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(confKey);
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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Long getPosition() {
        return position;
    }

    public void setPosition(Long position) {
        this.position = position;
    }

    public String getChannelData() {
        return channelData;
    }

    public void setChannelData(String channelData) {
        this.channelData = channelData;
    }

    public Long getImageMediaItemKey() {
        return imageMediaItemKey;
    }

    public void setImageMediaItemKey(Long imageMediaItemKey) {
        this.imageMediaItemKey = imageMediaItemKey;
    }

    public String getImageUniqueName() {
        return imageUniqueName;
    }

    public void setImageUniqueName(String imageUniqueName) {
        this.imageUniqueName = imageUniqueName;
    }

    public Long getSourceSATConfKey() {
        return sourceSATConfKey;
    }

    public void setSourceSATConfKey(Long sourceSATConfKey) {
        this.sourceSATConfKey = sourceSATConfKey;
    }
}
