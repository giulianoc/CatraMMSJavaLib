package com.catrammslib.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by multi on 08.06.18.
 */
public class CDN77ChannelConf implements Serializable{
    private Long confKey;
    private String label;
    private String rtmpURL;
    private String resourceURL;
    private String filePath;
	private String secureToken;
    private String type;
	private Long outputIndex;
    private Long reservedByIngestionJobKey;
	private String configurationLabel;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CDN77ChannelConf that = (CDN77ChannelConf) o;
		return confKey.equals(that.confKey);
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

	public Long getOutputIndex() {
		return outputIndex;
	}

	public void setOutputIndex(Long outputIndex) {
		this.outputIndex = outputIndex;
	}

	public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

	public String getRtmpURL() {
		return rtmpURL;
	}

	public void setRtmpURL(String rtmpURL) {
		this.rtmpURL = rtmpURL;
	}

	public String getResourceURL() {
		return resourceURL;
	}

	public void setResourceURL(String resourceURL) {
		this.resourceURL = resourceURL;
	}

	public String getConfigurationLabel() {
		return configurationLabel;
	}

	public void setConfigurationLabel(String configurationLabel) {
		this.configurationLabel = configurationLabel;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getSecureToken() {
		return secureToken;
	}

	public void setSecureToken(String secureToken) {
		this.secureToken = secureToken;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Long getReservedByIngestionJobKey() {
		return reservedByIngestionJobKey;
	}

	public void setReservedByIngestionJobKey(Long reservedByIngestionJobKey) {
		this.reservedByIngestionJobKey = reservedByIngestionJobKey;
	}

}
