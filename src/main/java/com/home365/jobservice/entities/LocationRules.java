package com.home365.jobservice.entities;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "LOCATION_RULES")
public class LocationRules {

    @Id
    String id;
    @Column(name = "location_name")
    String locationName;
    @Column
    String rules;
    @Column(name = "pm_account_id")
    String pmAccountId;
    @Transient
    private Rules rule;
}
