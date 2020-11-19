package com.home365.jobservice.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "Transactions")
@NoArgsConstructor
@AllArgsConstructor
public class TransactionsWithProjectedBalance {
    @Id
    private String transactionId;
    private double projected_balance;
    private double amount;
    private String propertyId;
    private String ChargeAccountId;
}
