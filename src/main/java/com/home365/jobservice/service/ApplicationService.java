package com.home365.jobservice.service;

import com.home365.jobservice.entities.Transactions;

import java.util.List;

public interface ApplicationService {
   List<Transactions> pendingStatusChange();

    boolean startLateFeeJob();
}
