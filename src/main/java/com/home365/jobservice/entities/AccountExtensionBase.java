package com.home365.jobservice.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "AccountExtensionBase")
@Getter
@Setter
@NoArgsConstructor
public class AccountExtensionBase {

    @Id
    @Column(name = "AccountId")
    private String accountId;

    @Column(name = "New_BusinessType")
    private int businessType;

    @Column(name = "New_StripeApiKey")
    private String stripeApiKey;

    @Column(name = "new_managerid")
    private String newManagerId;

    @Column(name = "New_StripeRentAccountId")
    private String stripeRentAccount;

    @Column(name = "New_StripeRentCustomerId")
    private String stripeCustomerAccount;

    @Column(name = "StripeExternalConnectedAccountId")
    private String stripeExternalAccount;

    @Column(name = "New_StripeAccountId")
    private String stripeAccountId;

    @Column(name = "New_StripeCustomerId")
    private String stripeCustomerId;

    @Column(name = "New_FullName")
    private String fullName;

    @Column(name = "StripeCustomerPayment")
    private String stripeCustomerPayment;

    @Column(name = "New_status")
    private int newStatus;

    @Column(name = "New_AccountType")
    private String accountType;

    @Column(name = "New_PlaidClientId")
    private String plaidClientId;

    @Column(name = "New_PlaidSecret")
    private String plaidSecretKey;


    @Column(name = "tenant_status")
    String tenantStatus;
    @Column(name = "New_PayeeMethod")
    private Integer payeeMethod;
}
