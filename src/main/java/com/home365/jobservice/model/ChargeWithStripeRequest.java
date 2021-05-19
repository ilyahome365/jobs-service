package com.home365.jobservice.model;

import lombok.Data;

import java.util.List;

@Data
public class ChargeWithStripeRequest {
    private long feeAmount;

    private List<String> charges;
    private List<String> refundTransactions;
    private Boolean sendMailFlag;
    private String description;
    private Boolean isRefunded;
}
