package com.home365.jobservice.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PendingStatusJobData {

    private Long readyForPayment = 0L;
    private Long pendingContribution = 0L;
    private Long failedToChange = 0L;
    private List<TransactionsFailedToChange> transactionsFailedToChanges = new ArrayList<>();

}
