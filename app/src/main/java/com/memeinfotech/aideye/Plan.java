package com.memeinfotech.aideye;

public class Plan {

    String name;
    String amount;
    String currency;
    String description;


    public Plan(String name,
                String amount, String currency, String description) {

        this.name = name;
        this.amount = amount;
        this.currency = currency;
        this.description = description;


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
