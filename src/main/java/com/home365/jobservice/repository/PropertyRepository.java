package com.home365.jobservice.repository;

import com.home365.jobservice.entities.IPlanInformation;
import com.home365.jobservice.entities.IPropertyLeaseInformationProjection;
import com.home365.jobservice.entities.PropertyExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface PropertyRepository extends JpaRepository<PropertyExtension, String> {


    @Query(value = "SELECT DISTINCT peb.New_ShortenAddress AS propertyName, " +
            "                       peb.New_Unit           AS unit, " +
            "                       peb.New_Building       AS building, " +
            "                       cb.FullName            AS tenantName, " +
            "                       pte.New_EndDate        AS endDate " +
            "FROM dbo.New_property_tenantExtensionBase pte " +
            "         INNER JOIN dbo.New_propertyaccountExtensionBase pa on pa.new_propertyid = pte.new_propertyid " +
            "         INNER JOIN New_propertyExtensionBase peb ON peb.New_propertyId = pte.new_propertyid " +
            "         INNER JOIN ContactBase cb ON cb.ContactId = pte.new_contactid " +
            "WHERE pa.new_accountid = 'F90E128A-CD00-4DF7-B0D0-0F40F80D623A' " +
            "  AND pte.New_PropertyUserType = 1 " +
            "  AND pte.New_IsActive = 1 " +
            "  AND [dbo].[GetTenentAccountIdByPropertyId](pte.new_propertyid) is not null " +
            "  AND pte.New_EndDate between :startDate and :endDate", nativeQuery = true)
    List<IPropertyLeaseInformationProjection> findAllForLeaseNotification(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query(value = "SELECT DISTINCT peb.New_ShortenAddress AS propertyName, " +
            "                       peb.New_Unit           AS unit, " +
            "                       peb.New_Building       AS building, " +
            "                       cb.FullName            AS tenantName, " +
            "                       pte.New_EndDate        AS endDate " +
            "FROM dbo.New_property_tenantExtensionBase pte " +
            "         INNER JOIN dbo.New_propertyaccountExtensionBase pa on pa.new_propertyid = pte.new_propertyid " +
            "         INNER JOIN New_propertyExtensionBase peb ON peb.New_propertyId = pte.new_propertyid " +
            "         INNER JOIN ContactBase cb ON cb.ContactId = pte.new_contactid " +
            "WHERE pa.new_accountid = 'F90E128A-CD00-4DF7-B0D0-0F40F80D623A' " +
            "  AND pte.New_PropertyUserType = 1 " +
            "  AND pte.New_IsActive = 1 " +
            "  AND [dbo].[GetTenentAccountIdByPropertyId](pte.new_propertyid) is not null ", nativeQuery = true)
    List<IPlanInformation> getAllActivePlans();
}
