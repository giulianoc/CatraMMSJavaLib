package com.catrammslib.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by multi on 08.06.18.
 */
public class IPChannelConf implements Serializable{

    private Long confKey;
    private String label;
    private String url;
    private String type;
    private String description;
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
        IPChannelConf that = (IPChannelConf) o;
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
