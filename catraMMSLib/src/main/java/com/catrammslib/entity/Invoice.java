package com.catrammslib.entity;

import com.catrammslib.utility.Cost;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by multi on 08.06.18.
 */
public class Invoice implements Serializable {
    private Long invoiceKey;
    private Long userKey;
    private Date creationDate;
    private String description;
    private Long amount;
    private Date expirationDate;
    private Boolean paid;
    private Date paymentDate;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invoice that = (Invoice) o;
        return invoiceKey.equals(that.invoiceKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoiceKey);
    }

    public Long getInvoiceKey() {
        return invoiceKey;
    }

    public void setInvoiceKey(Long invoiceKey) {
        this.invoiceKey = invoiceKey;
    }

    public Long getUserKey() {
        return userKey;
    }

    public void setUserKey(Long userKey) {
        this.userKey = userKey;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Boolean getPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }
}
