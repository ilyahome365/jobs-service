package com.home365.jobservice.service;

import com.home365.jobservice.entities.IPropertyLeaseInformation;
import com.home365.jobservice.entities.projection.IPropertyLeaseInformationProjection;
import com.home365.jobservice.entities.Recurring;
import com.home365.jobservice.model.JobExecutionResults;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface RecurringService {
    List<Recurring> findByActive(boolean isActive);

    Optional<Recurring> findById(String recurringId);

    Recurring save(Recurring recurring);

    JobExecutionResults createTransactionsForRecurringCharges();

    List<Recurring> findAllForLeaseNotification(Date startDate, Date endDate);

    List<IPropertyLeaseInformationProjection> getRecurrentPropertyAndTenantByRecurringIds(List<String> recurringIds);

    List<IPropertyLeaseInformation> getLeaseDatesByLeaseId(@Param("leaseId") String leaseId);
}
