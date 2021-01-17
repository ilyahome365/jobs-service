package com.home365.jobservice.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "New_propertyaccountExtensionBase")
@Getter
@Setter
@NoArgsConstructor
public class PropertyAccountExtension implements Serializable {

    @Id
    @Column(name = "New_propertyaccountId")
    private String propertyAccountId;
    @Column(name = "New_AccountId")
    private String accountId;
    @Column(name = "New_PropertyId")
    private String propertyId;
}
