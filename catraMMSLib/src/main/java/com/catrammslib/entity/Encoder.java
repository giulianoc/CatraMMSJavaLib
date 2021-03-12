package com.catrammslib.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by multi on 09.06.18.
 */
public class Encoder implements Serializable{
    private Long encoderKey;
    private String label;
    private Boolean external;
    private Boolean enabled;
    private String protocol;
    private String serverName;
    private Long port;

    private Long maxTranscodingCapability;
    private Long maxLiveProxiesCapabilities;
    private Long maxLiveRecordingCapabilities;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Encoder encoder = (Encoder) o;
        return encoderKey.equals(encoder.encoderKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(encoderKey);
    }

    public Long getEncoderKey() {
        return encoderKey;
    }

    public void setEncoderKey(Long encoderKey) {
        this.encoderKey = encoderKey;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public Long getPort() {
        return port;
    }

    public void setPort(Long port) {
        this.port = port;
    }

    public Long getMaxTranscodingCapability() {
        return maxTranscodingCapability;
    }

    public void setMaxTranscodingCapability(Long maxTranscodingCapability) {
        this.maxTranscodingCapability = maxTranscodingCapability;
    }

    public Long getMaxLiveProxiesCapabilities() {
        return maxLiveProxiesCapabilities;
    }

    public void setMaxLiveProxiesCapabilities(Long maxLiveProxiesCapabilities) {
        this.maxLiveProxiesCapabilities = maxLiveProxiesCapabilities;
    }

    public Long getMaxLiveRecordingCapabilities() {
        return maxLiveRecordingCapabilities;
    }

    public void setMaxLiveRecordingCapabilities(Long maxLiveRecordingCapabilities) {
        this.maxLiveRecordingCapabilities = maxLiveRecordingCapabilities;
    }

    public Boolean getExternal() {
        return external;
    }

    public void setExternal(Boolean external) {
        this.external = external;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
