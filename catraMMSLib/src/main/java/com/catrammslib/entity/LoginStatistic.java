package com.catrammslib.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by multi on 09.06.18.
 */
public class LoginStatistic implements Serializable{
	private Long loginStatisticKey;
	private String userName;
	private String emailAddress;
    private Long userKey;
	private String ip;
	private String continent;
	private String continentCode;
	private String country;
	private String countryCode;
	private String region;
	private String city;
	private String org;
	private String isp;
	private Date lastGEOUpdate;
	private Date lastTimeUsed;
    private Date successfulLogin;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((loginStatisticKey == null) ? 0 : loginStatisticKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LoginStatistic other = (LoginStatistic) obj;
		if (loginStatisticKey == null) {
			if (other.loginStatisticKey != null)
				return false;
		} else if (!loginStatisticKey.equals(other.loginStatisticKey))
			return false;
		return true;
	}

	public Long getLoginStatisticKey() {
		return loginStatisticKey;
	}

	public void setLoginStatisticKey(Long loginStatisticKey) {
		this.loginStatisticKey = loginStatisticKey;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public Long getUserKey() {
		return userKey;
	}

	public void setUserKey(Long userKey) {
		this.userKey = userKey;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getContinent() {
		return continent;
	}

	public void setContinent(String continent) {
		this.continent = continent;
	}

	public String getContinentCode() {
		return continentCode;
	}

	public void setContinentCode(String continentCode) {
		this.continentCode = continentCode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getOrg() {
		return org;
	}

	public void setOrg(String org) {
		this.org = org;
	}

	public String getIsp() {
		return isp;
	}

	public void setIsp(String isp) {
		this.isp = isp;
	}

	public Date getLastGEOUpdate() {
		return lastGEOUpdate;
	}

	public void setLastGEOUpdate(Date lastGEOUpdate) {
		this.lastGEOUpdate = lastGEOUpdate;
	}

	public Date getLastTimeUsed() {
		return lastTimeUsed;
	}

	public void setLastTimeUsed(Date lastTimeUsed) {
		this.lastTimeUsed = lastTimeUsed;
	}

	public Date getSuccessfulLogin() {
		return successfulLogin;
	}

	public void setSuccessfulLogin(Date successfulLogin) {
		this.successfulLogin = successfulLogin;
	}
}
