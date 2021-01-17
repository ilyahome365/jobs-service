package com.home365.jobservice.model;

import lombok.Data;

@Data
public class TransactionsFailedToChange {
    private String transactionId;
    private String reasonFailedToChange;
}
