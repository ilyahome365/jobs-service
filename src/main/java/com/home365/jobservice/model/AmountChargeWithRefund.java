package com.home365.jobservice.model;

import lombok.Data;

@Data
public class AmountChargeWithRefund {
    private Integer amountShouldBeCharged;
    private Integer actualPay;
}
