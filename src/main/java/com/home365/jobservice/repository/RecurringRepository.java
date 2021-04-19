package com.home365.jobservice.repository;

import com.home365.jobservice.entities.Recurring;
import com.home365.jobservice.entities.projection.IPropertyLeaseInformation;
import com.home365.jobservice.entities.projection.IPropertyLeaseInformationProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface RecurringRepository extends JpaRepository<Recurring, String> {

    List<Recurring> findByActiveAndPmAccountId(boolean isActive, String pmAccountId);

    @Query(value = "" +
            "SELECT * " +
            "FROM Recurring " +
            "WHERE BillType = 'Rent' " +
            "AND DueDate between :startDate and :endDate", nativeQuery = true)
    List<Recurring> findAllForLeaseNotification(Date startDate, Date endDate);

    @Query(value = "SELECT recurringTable.Id      AS recurrentId, " +
            "              peb.New_ShortenAddress AS propertyName, " +
            "              ab.Name                AS tenantName " +
            "FROM Recurring recurringTable " +
            "         INNER JOIN New_propertyExtensionBase peb ON peb.New_propertyId = recurringTable.PropertyId " +
            "         INNER JOIN New_property_tenantExtensionBase pte ON pte.New_propertyId = recurringTable.PropertyId " +
            "         inner join dbo.New_contactaccountExtensionBase ca on ca.New_ContactId = pte.New_ContactId " +
            "         inner join dbo.New_contactaccountBase cab on ca.New_contactaccountId = cab.New_contactaccountId " +
            "         inner join dbo.AccountExtensionBase a on a.AccountId = ca.New_AccountId " +
            "         inner join dbo.AccountBase ab on ab.AccountId = ca.New_AccountId " +
            "WHERE recurringTable.Id IN (:recurrentIds) " +
            "  and pte.New_IsActive = 1 " +
            "  and pte.New_PropertyUserType = 1 " +
            "  and cab.statuscode = 1 " +
            "  and a.New_BusinessType = 10 ", nativeQuery = true)
    List<IPropertyLeaseInformationProjection> getRecurrentPropertyAndTenantByRecurringIds(@Param("recurrentIds") List<String> recurringIds);

    @Query(value = "select New_StartDate as startDate, New_EndDate as endDate, moveOutDate as moveOutDate from New_property_tenantExtensionBase where New_property_tenantId = :leaseId",
    nativeQuery = true)
    List<IPropertyLeaseInformation> getLeaseDatesByLeaseId(@Param("leaseId") String leaseId);
}
