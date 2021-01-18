package com.home365.jobservice.model;

import lombok.Data;

@Data
public class PaymentResults {
    private int all;
    private int failed;
    private int  test;
    private String msg;

}
