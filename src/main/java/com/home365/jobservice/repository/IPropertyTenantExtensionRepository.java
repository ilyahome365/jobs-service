package com.home365.jobservice.repository;

import com.home365.jobservice.entities.PropertyTenantExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IPropertyTenantExtensionRepository extends JpaRepository<PropertyTenantExtension, String> {

    @Query(value = "SELECT * " +
            "FROM dbo.New_property_tenantExtensionBase pte " +
            "         INNER JOIN dbo.New_propertyaccountExtensionBase pa on pa.new_propertyid = pte.new_propertyid " +
            "         INNER JOIN New_propertyExtensionBase peb ON peb.New_propertyId = pte.new_propertyid " +
            "         INNER JOIN ContactBase cb ON cb.ContactId = pte.new_contactid " +
            "WHERE pa.new_accountid =:locationId " +
            "  AND pte.New_PropertyUserType = 1 " +
            "  AND pte.New_IsActive = 1 " +
            "  AND [dbo].[GetTenentAccountIdByPropertyId](pte.new_propertyid) is not null ", nativeQuery = true)
    List<PropertyTenantExtension> getAllActivePlansToUpdate(@Param("locationId") String locationId);
}
