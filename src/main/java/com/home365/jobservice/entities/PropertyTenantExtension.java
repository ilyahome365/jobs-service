package com.home365.jobservice.entities;

import com.home365.jobservice.entities.enums.EntityType;
import com.home365.jobservice.entities.enums.LeaseType;
import com.home365.jobservice.entities.projection.IAuditableEntity;
import com.home365.jobservice.service.AuditInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "New_property_tenantExtensionBase")
@Getter
@Setter
@NoArgsConstructor
public class PropertyTenantExtension implements IAuditableEntity {

    @Id
    @Column(name = "New_property_tenantId")
    @AuditInfo(ignore = true)
    private String propertyTenantId;

    @Column(name = "new_propertyid")
    @AuditInfo(ignore = true)
    private String propertyId;

    @Column(name = "new_contactid")
    @AuditInfo(ignore = true)
    private String contactId;

    @Column(name = "New_RentAmount")
    private Long rentAmount;

    @Column(name = "New_IsActive")
    private Boolean active;

    @Column(name = "New_PropertyUserType")
    @AuditInfo(ignore = true)
    private Integer userType;

    @Column(name = "New_EndDate")
    private LocalDateTime endDate;

    @Column(name = "New_StartDate")
    private LocalDateTime startDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "lease_type")
    private LeaseType leaseType;

    @Column(name = "days_left")
    private Integer daysLeft;

    @Column(name = "moveOutDate")
    private LocalDate moveOutDate;

    @Column(name = "New_InactiveDate")
    @AuditInfo(ignore = true)
    private LocalDate inactiveDate;

    @Column(name = "new_propertyaccountid")
    @AuditInfo(ignore = true)
    private String propertAccountId;

    @Override
    public EntityType auditEntityType() {
        return EntityType.Property;
    }

    @Override
    public String auditEntityIdentifier() {
        return this.propertyId;
    }

    @Override
    public String auditMessage() {
        return null;
    }

    @Override
    public String idOfEntity() {
        return this.propertyTenantId;
    }
}
