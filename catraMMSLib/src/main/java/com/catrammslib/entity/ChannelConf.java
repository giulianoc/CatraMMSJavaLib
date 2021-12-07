package com.catrammslib.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by multi on 08.06.18.
 */
public class ChannelConf implements Serializable{

    private Long confKey;
    private String label;
	private String encodersPoolLabel;
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

	private String sourceType;

	private String pushProtocol;
	private String pushServerName;
	private Long pushServerPort;
	private String pushURI;
	private Long pushListenTimeout;
	private Long captureLiveVideoDeviceNumber;
	private String captureLiveVideoInputFormat;
	private Long captureLiveFrameRate;
	private Long captureLiveWidth;
	private Long captureLiveHeight;
	private Long captureLiveAudioDeviceNumber;
	private Long captureLiveChannelsNumber;
	private Long sourceSATConfKey;

	private Boolean selected;

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChannelConf that = (ChannelConf) o;
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

    public Boolean getSelected() {
		return selected;
	}

	public void setSelected(Boolean selected) {
		this.selected = selected;
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

    public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public String getPushProtocol() {
		return pushProtocol;
	}

	public void setPushProtocol(String pushProtocol) {
		this.pushProtocol = pushProtocol;
	}

	public Long getPushServerPort() {
		return pushServerPort;
	}

	public void setPushServerPort(Long pushServerPort) {
		this.pushServerPort = pushServerPort;
	}

	public String getPushURI() {
		return pushURI;
	}

	public void setPushURI(String pushURI) {
		this.pushURI = pushURI;
	}

	public Long getPushListenTimeout() {
		return pushListenTimeout;
	}

	public void setPushListenTimeout(Long pushListenTimeout) {
		this.pushListenTimeout = pushListenTimeout;
	}

	public Long getCaptureLiveVideoDeviceNumber() {
		return captureLiveVideoDeviceNumber;
	}

	public void setCaptureLiveVideoDeviceNumber(Long captureLiveVideoDeviceNumber) {
		this.captureLiveVideoDeviceNumber = captureLiveVideoDeviceNumber;
	}

	public String getCaptureLiveVideoInputFormat() {
		return captureLiveVideoInputFormat;
	}

	public void setCaptureLiveVideoInputFormat(String captureLiveVideoInputFormat) {
		this.captureLiveVideoInputFormat = captureLiveVideoInputFormat;
	}

	public Long getCaptureLiveFrameRate() {
		return captureLiveFrameRate;
	}

	public void setCaptureLiveFrameRate(Long captureLiveFrameRate) {
		this.captureLiveFrameRate = captureLiveFrameRate;
	}

	public Long getCaptureLiveWidth() {
		return captureLiveWidth;
	}

	public void setCaptureLiveWidth(Long captureLiveWidth) {
		this.captureLiveWidth = captureLiveWidth;
	}

	public Long getCaptureLiveHeight() {
		return captureLiveHeight;
	}

	public void setCaptureLiveHeight(Long captureLiveHeight) {
		this.captureLiveHeight = captureLiveHeight;
	}

	public Long getCaptureLiveAudioDeviceNumber() {
		return captureLiveAudioDeviceNumber;
	}

	public void setCaptureLiveAudioDeviceNumber(Long captureLiveAudioDeviceNumber) {
		this.captureLiveAudioDeviceNumber = captureLiveAudioDeviceNumber;
	}

	public Long getCaptureLiveChannelsNumber() {
		return captureLiveChannelsNumber;
	}

	public void setCaptureLiveChannelsNumber(Long captureLiveChannelsNumber) {
		this.captureLiveChannelsNumber = captureLiveChannelsNumber;
	}

	public Long getSourceSATConfKey() {
		return sourceSATConfKey;
	}

	public void setSourceSATConfKey(Long sourceSATConfKey) {
		this.sourceSATConfKey = sourceSATConfKey;
	}

	public String getPushServerName() {
		return pushServerName;
	}

	public void setPushServerName(String pushServerName) {
		this.pushServerName = pushServerName;
	}

	public String getEncodersPoolLabel() {
		return encodersPoolLabel;
	}

	public void setEncodersPoolLabel(String encodersPoolLabel) {
		this.encodersPoolLabel = encodersPoolLabel;
	}

	public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
