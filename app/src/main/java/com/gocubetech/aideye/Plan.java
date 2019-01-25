package com.gocubetech.aideye;

import java.io.Serializable;
public class Plan implements Serializable {

    String name;
    String amount;
    String currency;
    String description;
    String planId;

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public Plan(String name,
                String amount, String currency, String description, String planId) {
        this.name = name;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.planId = planId;

    }

    public Plan() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
