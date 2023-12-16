package com.catrammslib.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by multi on 08.06.18.
 */
public class UserProfile implements Serializable {
    private Long userKey;
    private Boolean ldapEnabled;
    private String name;
    private String email;
    private String password;
    private String country;
    private Date creationDate;
    private Boolean insolvent;
    private Date expirationDate;
    private String creditCard_cardNumber;
    private String creditCard_nameOnCard;
    private Date creditCard_expiryDate;
    private String creditCard_securityCode;

    public Long getUserKey() {
        return userKey;
    }

    public void setUserKey(Long userKey) {
        this.userKey = userKey;
    }

    public Boolean getLdapEnabled() {
        return ldapEnabled;
    }

    public void setLdapEnabled(Boolean ldapEnabled) {
        this.ldapEnabled = ldapEnabled;
    }

    public String getCreditCard_cardNumber() {
        return creditCard_cardNumber;
    }

    public void setCreditCard_cardNumber(String creditCard_cardNumber) {
        this.creditCard_cardNumber = creditCard_cardNumber;
    }

    public String getCreditCard_nameOnCard() {
        return creditCard_nameOnCard;
    }

    public void setCreditCard_nameOnCard(String creditCard_nameOnCard) {
        this.creditCard_nameOnCard = creditCard_nameOnCard;
    }

    public Date getCreditCard_expiryDate() {
        return creditCard_expiryDate;
    }

    public void setCreditCard_expiryDate(Date creditCard_expiryDate) {
        this.creditCard_expiryDate = creditCard_expiryDate;
    }

    public String getCreditCard_securityCode() {
        return creditCard_securityCode;
    }

    public void setCreditCard_securityCode(String creditCard_securityCode) {
        this.creditCard_securityCode = creditCard_securityCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Boolean getInsolvent() {
        return insolvent;
    }

    public void setInsolvent(Boolean insolvent) {
        this.insolvent = insolvent;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }
}
