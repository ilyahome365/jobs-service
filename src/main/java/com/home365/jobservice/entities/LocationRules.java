package com.home365.jobservice.entities;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
}
