package com.catrammslib.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by multi on 08.06.18.
 */
public class Stream implements Serializable{

    private Long confKey;
    private String label;
	private Long encodersPoolKey;
	private String encodersPoolLabel;	// calcolato
    private String url;
    private String type;
    private String description;
    private String name;
    private String region;
    private String country;
    private Long imageMediaItemKey;
    private String imageUniqueName;
    private Long position;
    private String userData;

	private String sourceType;

	private String pushProtocol;
	private Long pushEncoderKey;
	private Boolean pushPublicEncoderName; // encoderKey non è sufficiente, pushEncoderName indica il nome del server publico o privato
	private String pushEncoderLabel;	// this is a calculated field
	private String pushEncoderName;	// this is a calculated field
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
	private Long tvSourceTVConfKey;

	private Boolean selected;

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stream that = (Stream) o;
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

    public Long getPushEncoderKey() {
		return pushEncoderKey;
	}

	public void setPushEncoderKey(Long pushEncoderKey) {
		this.pushEncoderKey = pushEncoderKey;
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

	public String getUserData() {
		return userData;
	}

	public void setUserData(String userData) {
		this.userData = userData;
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

	public Boolean getPushPublicEncoderName() {
		return pushPublicEncoderName;
	}

	public void setPushPublicEncoderName(Boolean pushPublicEncoderName) {
		this.pushPublicEncoderName = pushPublicEncoderName;
	}

	public String getPushEncoderName() {
		return pushEncoderName;
	}

	public void setPushEncoderName(String pushEncoderName) {
		this.pushEncoderName = pushEncoderName;
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

	public Long getTvSourceTVConfKey() {
		return tvSourceTVConfKey;
	}

	public void setTvSourceTVConfKey(Long tvSourceTVConfKey) {
		this.tvSourceTVConfKey = tvSourceTVConfKey;
	}

	public String getPushEncoderLabel() {
		return pushEncoderLabel;
	}

	public void setPushEncoderLabel(String pushEncoderLabel) {
		this.pushEncoderLabel = pushEncoderLabel;
	}

	public Long getEncodersPoolKey() {
		return encodersPoolKey;
	}

	public void setEncodersPoolKey(Long encodersPoolKey) {
		this.encodersPoolKey = encodersPoolKey;
	}
}
