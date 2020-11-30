package com.home365.jobservice.repository;

import com.home365.jobservice.entities.PropertyTenantExtension;
import com.home365.jobservice.entities.projection.ILeaseInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IPropertyTenantExtensionRepository extends JpaRepository<PropertyTenantExtension, String> {

    @Query(value = "SELECT DISTINCT cb.FullName               AS tenantName, " +
            "                       pte.New_EndDate           AS endDate, " +
            "                       pte.lease_type            AS leaseType," +
            "                       pte.New_property_tenantId AS propertyTenantId " +
            "FROM dbo.New_property_tenantExtensionBase pte " +
            "         INNER JOIN dbo.New_propertyaccountExtensionBase pa on pa.new_propertyid = pte.new_propertyid " +
            "         INNER JOIN New_propertyExtensionBase peb ON peb.New_propertyId = pte.new_propertyid " +
            "         INNER JOIN ContactBase cb ON cb.ContactId = pte.new_contactid " +
            "WHERE pa.new_accountid = 'F90E128A-CD00-4DF7-B0D0-0F40F80D623A' " +
            "  AND pte.New_PropertyUserType = 1 " +
            "  AND pte.New_IsActive = 1 " +
            "  AND [dbo].[GetTenentAccountIdByPropertyId](pte.new_propertyid) is not null ", nativeQuery = true)
    List<ILeaseInformation> getAllActivePlans();
}
