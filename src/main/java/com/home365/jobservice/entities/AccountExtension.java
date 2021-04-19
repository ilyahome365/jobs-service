package com.home365.jobservice.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "AccountBase")
@Getter
@Setter
@NoArgsConstructor
public class AccountExtension {

    @Id
    @Column(name = "AccountId")
    private String accountId;

    @Column(name = "Name")
    private String name;

    @Column(name = "EMailAddress1")
    private String email;

    @Column(name = "Telephone1")
    private String phone;
}
