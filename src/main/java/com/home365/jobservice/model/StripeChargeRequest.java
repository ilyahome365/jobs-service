package com.home365.jobservice.model;

public class StripeChargeRequest {

    public enum Currency {
        EUR, USD;
    }

    private String stripeCustomerId;
    private int amount;
    private String destinationStripeAccountId;
    private String description;
    private boolean addAchFee;
    private String stripeLocationKey;

    public String getStripeCustomerId() {
        return stripeCustomerId;
    }

    public void setStripeCustomerId(String stripeCustomerId) {
        this.stripeCustomerId = stripeCustomerId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getDestinationStripeAccountId() {
        return destinationStripeAccountId;
    }

    public void setDestinationStripeAccountId(String destinationStripeAccountId) {
        this.destinationStripeAccountId = destinationStripeAccountId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAddAchFee() {
        return addAchFee;
    }

    public void setAddAchFee(boolean addAchFee) {
        this.addAchFee = addAchFee;
    }

    public String getStripeLocationKey() {
        return stripeLocationKey;
    }

    public void setStripeLocationKey(String stripeLocationKey) {
        this.stripeLocationKey = stripeLocationKey;
    }
}
