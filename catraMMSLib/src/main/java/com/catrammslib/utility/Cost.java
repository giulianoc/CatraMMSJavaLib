package com.catrammslib.utility;

import java.io.Serializable;

/**
 * Created by multi on 13.06.18.
 */
public class Cost implements Serializable {

    private String description;
    private Long monthlyCost;    // eur
    private Long stepFactor;
    private Long minAmount;
    private Long maxAmount;

    // i prossimi campi sono usati dalla GUI
    private Long currentAmount;
    private Long newAmount;

    public Cost clone()
    {
        Cost cost = new Cost();

        cost.setDescription(getDescription());
        cost.setMonthlyCost(getMonthlyCost());
        cost.setStepFactor(getStepFactor());
        cost.setMinAmount(getMinAmount());
        cost.setMaxAmount(getMaxAmount());
        cost.setCurrentAmount(getCurrentAmount());
        cost.setNewAmount(getNewAmount());

        return cost;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getStepFactor() {
        return stepFactor;
    }

    public void setStepFactor(Long stepFactor) {
        this.stepFactor = stepFactor;
    }

    public Long getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(Long minAmount) {
        this.minAmount = minAmount;
    }

    public Long getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(Long maxAmount) {
        this.maxAmount = maxAmount;
    }

    public Long getMonthlyCost() {
        return monthlyCost;
    }

    public void setMonthlyCost(Long monthlyCost) {
        this.monthlyCost = monthlyCost;
    }

    public Long getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(Long currentAmount) {
        this.currentAmount = currentAmount;
    }

    public Long getNewAmount() {
        return newAmount;
    }

    public void setNewAmount(Long newAmount) {
        this.newAmount = newAmount;
    }
}
