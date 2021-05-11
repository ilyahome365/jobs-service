package com.home365.jobservice.model;

import lombok.Data;

import java.util.List;

@Data
public class PaymentNotification {

    List<String> chargeIds;
}
