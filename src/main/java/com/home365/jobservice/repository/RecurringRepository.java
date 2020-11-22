package com.home365.jobservice.repository;

import com.home365.jobservice.entities.RecurrentPropertyTenantProjection;
import com.home365.jobservice.entities.Recurring;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface RecurringRepository extends JpaRepository<Recurring, String> {

    List<Recurring> findByActive(boolean isActive);

    @Query(value = "" +
            "SELECT * " +
            "FROM Recurring " +
            "WHERE BillType = 'Rent' " +
            "AND DueDate between :startDate and :endDate", nativeQuery = true)
    List<Recurring> findAllForLeaseNotification(Date startDate, Date endDate);

    @Query(value = "", nativeQuery = true)
    List<RecurrentPropertyTenantProjection> getRecurrentPropertyAndTenantByRecurringIds(List<String> recurringIds);
}
