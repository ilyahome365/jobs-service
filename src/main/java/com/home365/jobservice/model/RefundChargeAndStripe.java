package com.home365.jobservice.model;

import lombok.Data;

@Data
public class RefundChargeAndStripe {
    private String chargeAccount;
    private String reciveAccount;
    private Integer amount;
}
