package com.home365.jobservice.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "New_propertyExtensionBase")
@Getter
@Setter
@NoArgsConstructor
public class PropertyExtension {

    @Id
    @Column(name = "New_propertyId")
    private String propertyId;
    @Column(name = "New_name")
    private String name;
    @Column(name = "New_ShortenAddress")
    private String address;
    @Column(name = "New_Unit")
    private String unit;
    @Column(name = "New_Building")
    private String building;

}
