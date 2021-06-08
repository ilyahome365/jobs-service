package com.home365.jobservice.model;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Data
public class TransactionsDetails {
    private List<String> transactionNumberPaid;
    private List<String> transactionsNumberDidntPaid;

    public void setTransactionNumberPaid(List<String> transactionNumberPaid) {
        if (CollectionUtils.isEmpty(this.transactionNumberPaid))
            this.transactionNumberPaid = transactionNumberPaid;
        else {
            this.transactionNumberPaid.addAll(transactionNumberPaid);
        }
    }

    public void setTransactionsNumberDidntPaid(List<String> transactionsNumberDidntPaid) {
        if (CollectionUtils.isEmpty(this.transactionsNumberDidntPaid))
            this.transactionsNumberDidntPaid = transactionsNumberDidntPaid;
        else
            this.transactionsNumberDidntPaid.addAll(transactionsNumberDidntPaid);
    }
}
