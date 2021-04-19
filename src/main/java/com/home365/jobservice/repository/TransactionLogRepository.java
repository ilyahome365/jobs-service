package com.home365.jobservice.repository;

import com.home365.jobservice.entities.TransactionsLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionsLog, String> {
}
