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
    private String publicServerName;
    private String internalServerName;
    private Long port;

    /*
    private Long maxTranscodingCapability;
    private Long maxLiveProxiesCapabilities;
    private Long maxLiveRecordingCapabilities;
     */

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


    public String getPublicServerName() {
		return publicServerName;
	}

	public void setPublicServerName(String publicServerName) {
		this.publicServerName = publicServerName;
	}

	public String getInternalServerName() {
		return internalServerName;
	}

	public void setInternalServerName(String internalServerName) {
		this.internalServerName = internalServerName;
	}

	public Long getPort() {
        return port;
    }

    public void setPort(Long port) {
        this.port = port;
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
