package com.home365.jobservice.repository;

import com.home365.jobservice.entities.CashPaymentTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CashPaymentTrackingRepository extends JpaRepository<CashPaymentTracking, String> {
    @Override
    Optional<CashPaymentTracking> findById(String id);

    Optional<CashPaymentTracking> findByPaysafeId(String paysafeId);
}
