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
    @Transient
    private Rules rule;
}
