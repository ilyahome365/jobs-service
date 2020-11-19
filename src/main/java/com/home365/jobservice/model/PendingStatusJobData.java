package com.home365.jobservice.model;

import lombok.Data;

@Data
public class PendingStatusJobData {

    Long readyForPayment = 0L;
    Long pendingContribution = 0L;
}
