package com.home365.jobservice.model;

import lombok.Data;

@Data
public class PayBillJobWrapper  {
    PaymentResults checks;
    PaymentResults loans;
    PaymentResults eft;
    PaymentResults noPayment;

}
