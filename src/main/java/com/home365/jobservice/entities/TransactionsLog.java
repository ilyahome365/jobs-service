package com.home365.jobservice.entities;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "TransactionLog")
@Data
public class TransactionsLog {
    @Id
    private String transactionLogId ;
    private String transactionId;
    private LocalDate date;
    private String contactAccountId;
    private Integer eventName;
    private String argument;

}
