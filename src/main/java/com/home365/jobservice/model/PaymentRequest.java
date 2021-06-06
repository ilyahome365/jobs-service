package com.home365.jobservice.model;


import com.home365.jobservice.entities.enums.PaymentMethod;
import lombok.Data;



@Data
public class PaymentRequest {

    PaymentMethod paymentMethod;
    String reference;
    String description;
}
