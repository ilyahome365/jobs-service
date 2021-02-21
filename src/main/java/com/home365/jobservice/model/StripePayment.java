package com.home365.jobservice.model;

import lombok.Data;

@Data
public class StripePayment {
    String id;
    String transfer;
}
