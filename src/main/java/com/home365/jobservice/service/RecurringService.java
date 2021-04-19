package com.home365.jobservice.service;

import com.home365.jobservice.entities.projection.IPropertyLeaseInformation;
import com.home365.jobservice.entities.Recurring;
import com.home365.jobservice.entities.projection.IPropertyLeaseInformationProjection;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface RecurringService extends FindByIdAudit {
    List<Recurring> findByActive(boolean isActive, String pmAccountId);

    Optional<Recurring> findById(String recurringId);

    Recurring save(Recurring recurring);

    String createTransactionsForRecurringCharges(String lvPmAccountId);

    List<Recurring> findAllForLeaseNotification(Date startDate, Date endDate);

    List<IPropertyLeaseInformationProjection> getRecurrentPropertyAndTenantByRecurringIds(List<String> recurringIds);

    List<IPropertyLeaseInformation> getLeaseDatesByLeaseId(@Param("leaseId") String leaseId);

}
