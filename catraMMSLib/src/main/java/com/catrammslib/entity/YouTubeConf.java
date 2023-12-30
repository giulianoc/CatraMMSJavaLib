package com.catrammslib.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by multi on 08.06.18.
 */
public class YouTubeConf implements Serializable{

    private Long confKey;
    private String label;
    private String tokenType;
    private String refreshToken;
    private String accessToken;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YouTubeConf that = (YouTubeConf) o;
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

    public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
