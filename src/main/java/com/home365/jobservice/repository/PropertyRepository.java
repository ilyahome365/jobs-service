package com.home365.jobservice.repository;

import com.home365.jobservice.entities.IPropertyLeaseInformationProjection;
import com.home365.jobservice.entities.PropertyExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface PropertyRepository extends JpaRepository<PropertyExtension, String> {


    @Query(value = "SELECT peb.New_ShortenAddress AS propertyName, " +
            "              pt.New_TenantName      AS tenantName, " +
            "              pt.New_EndDate         AS endDate " +
            "       FROM dbo.New_property_tenantExtensionBase pte " +
            "         INNER JOIN dbo.New_propertyaccountExtensionBase pa on pa.new_propertyid = pte.new_propertyid " +
            "         INNER JOIN New_propertyExtensionBase peb ON peb.New_propertyId = pte.new_propertyid " +
            "         INNER JOIN New_property_tenant pt ON pt.New_propertyId = pte.new_propertyid " +
            "WHERE pa.new_accountid = 'F90E128A-CD00-4DF7-B0D0-0F40F80D623A' " +
            "  AND pt.New_PropertyUserType = 1 " +
            "  AND pt.New_IsActive = 1 " +
            "  AND [dbo].[GetTenentAccountIdByPropertyId](pt.new_propertyid) is not null " +
            "  AND pt.New_EndDate between :startDate and :endDate", nativeQuery = true)
    List<IPropertyLeaseInformationProjection> findAllForLeaseNotification(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
