package com.home365.jobservice.entities;

import com.home365.jobservice.entities.enums.LeaseType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "New_property_tenantExtensionBase")
@Getter
@Setter
@NoArgsConstructor
public class PropertyTenantExtension {

    @Id
    @Column(name = "New_property_tenantId")
    private String propertyTenantId;

    @Column(name = "new_propertyid")
    private String propertyId;

    @Column(name = "New_RentAmount")
    private Long rentAmount;

    @Column(name = "New_IsActive")
    private Boolean active;

    @Column(name = "New_PropertyUserType")
    private Integer userType;

    @Column(name = "New_EndDate")
    private Date endDate;

    @Column(name = "New_StartDate")
    private Date startDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "lease_type")
    private LeaseType leaseType;

    @Column(name = "days_left")
    private Integer daysLeft;
}
