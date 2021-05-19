package com.home365.jobservice.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ChargeWithStripeRequest implements Serializable {
    private long feeAmount;

    private List<String> charges;
    private List<String> refundTransactions;
    private Boolean sendMailFlag;
    private String description;
    private Boolean isRefunded;
}
