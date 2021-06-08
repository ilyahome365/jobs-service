package com.home365.jobservice.flow;


import com.home365.jobservice.config.AppProperties;
import com.home365.jobservice.entities.PropertyExtension;
import com.home365.jobservice.entities.enums.PropertyStatus;
import com.home365.jobservice.entities.enums.ReasonForLeavingProperty;
import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.model.AccountBalance;
import com.home365.jobservice.model.PropertyPhasingOutWrapper;
import com.home365.jobservice.model.RecipientMail;
import com.home365.jobservice.model.enums.TenantStatus;
import com.home365.jobservice.model.mail.MailDetails;
import com.home365.jobservice.model.mail.MailResult;
import com.home365.jobservice.model.wrapper.OwnerBillsWrapper;
import com.home365.jobservice.model.wrapper.OwnerWrapper;
import com.home365.jobservice.model.wrapper.TenantWrapper;
import com.home365.jobservice.rest.BalanceServiceExternal;
import com.home365.jobservice.rest.TenantServiceExternal;
import com.home365.jobservice.service.MailService;
import com.home365.jobservice.service.PropertyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PropertyPhasingOutFlow implements PropertyPhasingOut {

    public static final String PROPERTY_PHASING_OUT = " property phasing out ";
    public static final String PROPERTY_DEACTIVATION = " property deactivation ";
    private final BalanceServiceExternal balanceServiceExternal;
    private final PropertyService propertyService;
    private final TenantServiceExternal tenantServiceExternal;
    private final AppProperties appProperties;
    private final MailService mailService;


    public PropertyPhasingOutFlow(BalanceServiceExternal balanceServiceExternal, PropertyService propertyService, TenantServiceExternal tenantServiceExternal, AppProperties appProperties, MailService mailService) {
        this.balanceServiceExternal = balanceServiceExternal;
        this.propertyService = propertyService;
        this.tenantServiceExternal = tenantServiceExternal;
        this.appProperties = appProperties;
        this.mailService = mailService;
    }

    @Override
    public void startPropertyPhasingOut(String propertyId) throws GeneralException {
        log.info("Start property phasing out for property : {} ", propertyId);

        PropertyPhasingOutWrapper propertyPhasingOutWrapper = new PropertyPhasingOutWrapper();
        Optional<PropertyExtension> property = propertyService.findPropertyById(propertyId);

        if (property.isEmpty()) {
            GeneralException generalException = new GeneralException();
            generalException.setMessage("No property for " + propertyId);
            throw generalException;
        }
        if (property.get().getPropertyStatus() == null || !property.get().getPropertyStatus().equalsIgnoreCase(PropertyStatus.phasingOut.name())) {
            GeneralException generalException = new GeneralException();
            generalException.setMessage("property is not on property phase out");
            throw generalException;
        }
        List<TenantWrapper> tenants = tenantServiceExternal.getTenantsByPropertyId(propertyId);
        propertyPhasingOutWrapper.setPropertyId(propertyId);
        propertyPhasingOutWrapper.setTriggerDateAndTime(property.get().getPhasingOutDate().toString());
        cancelFutureBills(propertyPhasingOutWrapper);
        cancelFutureCharges(propertyPhasingOutWrapper);
        cancelRecurringForProperty(propertyPhasingOutWrapper, tenants);
        createOwnerBillFromTenantCharges(propertyPhasingOutWrapper, tenants);
        if (!property.get().getReasonForLeaving().equalsIgnoreCase(ReasonForLeavingProperty.SoldByHome365_PropertyNotStaysInHome365.name()) &&
                !property.get().getReasonForLeaving().equalsIgnoreCase(ReasonForLeavingProperty.SoldByHome365_PropertyStaysInHome365.name())) {
            String createdBill = createTearminationFeeBill(propertyId, PROPERTY_PHASING_OUT);
            log.info("Created BIll Id : {} ", createdBill);
        }

        String materialTransferFee = createMaterialTransferFee(propertyId, PROPERTY_PHASING_OUT);
        log.info("Material transfer fee bill id : {} ", materialTransferFee);

        balanceServiceExternal.moveSecurityDepositToOwnerAccount(propertyId," Moving deposit due property phase out" );
        OwnerWrapper ownerFromProperty = tenantServiceExternal.getOwnerFromProperty(propertyId);
        AccountBalance ownerProjectedBalance = balanceServiceExternal.getOwnerProjectedBalance(ownerFromProperty.getAccountId());
        Double projectedBalance = ownerProjectedBalance.getBalance();
        log.info("Projected balance for account : {}  is {} ", ownerFromProperty.getAccountId(), projectedBalance);
        if (!projectedBalance.isNaN()) {
            if (projectedBalance >= 0) {
                createAndSendMail(ownerFromProperty, "Property phasing out", property.get());
            }
        }

        tenantServiceExternal.movePropertyToReadyForDeactivation(propertyId);


    }

    private void createAndSendMail(OwnerWrapper ownerFromProperty, String subject, PropertyExtension property) {
        log.info("send mail to : {}  with subject {} ", ownerFromProperty.getEmail(), subject);
        MailDetails mailDetails = new MailDetails();
        mailDetails.setFrom(appProperties.getMailSupport());
        mailDetails.setSubject(subject);
        mailDetails.setTemplateName("property-phasing-out-owners");
        RecipientMail recipientMail = RecipientMail.builder()
                .name(ownerFromProperty.getName())
                .email(ownerFromProperty.getEmail())
                .build();
        RecipientMail localRecipient = RecipientMail.builder()
                .name("HOME365 Support")
                .email(appProperties.getMailSupport())
                .build();
        mailDetails.setRecipients(List.of(recipientMail, localRecipient));
        mailDetails.setContentTemplate(getContentTemplate(property, ownerFromProperty));
        MailResult mailResult = mailService.sendMail(mailDetails);
        log.info("mail log result : {} ", mailResult);
    }

    private Map<String, String> getContentTemplate(PropertyExtension property, OwnerWrapper ownerFromProperty) {
        Map<String, String> contentTemplate = new HashMap<>();
        contentTemplate.put("OWNER_NAME", ownerFromProperty.getName());
        contentTemplate.put("ADDRESS", property.getAddress());
        return contentTemplate;
    }


    private String createMaterialTransferFee(String propertyId, String businessAction) throws GeneralException {
        OwnerBillsWrapper ownerBillsWrapper = new OwnerBillsWrapper();
        ownerBillsWrapper.setPropertyId(propertyId);
        return balanceServiceExternal.createMaterialTransferFee(ownerBillsWrapper,businessAction );
    }

    private String createTearminationFeeBill(String propertyId, String businessAction) throws GeneralException {
        log.info("Create Termination Fee for propertyId : {} ", propertyId);
        OwnerBillsWrapper ownerBillsWrapper = new OwnerBillsWrapper();
        ownerBillsWrapper.setPropertyId(propertyId);
        return balanceServiceExternal.createTerminationFeeBill(ownerBillsWrapper,businessAction );
    }

    private void createOwnerBillFromTenantCharges(PropertyPhasingOutWrapper propertyPhasingOutWrapper, List<TenantWrapper> tenants) throws GeneralException {
        OwnerBillsWrapper ownerBillsWrapper = new OwnerBillsWrapper();
        Optional<TenantWrapper> tenant = tenants.stream().filter(tenantWrapper -> tenantWrapper.getTenantStatus().equals(TenantStatus.Active)).findFirst();
        if (tenant.isPresent()) {
            ownerBillsWrapper.setChargeAccount(tenant.get().getAccountId());
            ownerBillsWrapper.setPropertyId(propertyPhasingOutWrapper.getPropertyId());
            balanceServiceExternal.createOwnerBillForTenantDebts(ownerBillsWrapper, PROPERTY_PHASING_OUT);
        }
    }

    private void cancelRecurringForProperty(PropertyPhasingOutWrapper propertyPhasingOutWrapper, List<TenantWrapper> tenants) throws GeneralException {
        log.info("start cancel recurring charges for : {} ", propertyPhasingOutWrapper);

        List<String> canceledRecurring = new ArrayList<>();
        if (!CollectionUtils.isEmpty(tenants)) {
            List<TenantWrapper> activeTenants = tenants.stream().filter(tenantWrapper -> !tenantWrapper.getTenantStatus().equals(TenantStatus.Inactive)).collect(Collectors.toList());
            for (TenantWrapper tenantWrapper : activeTenants) {
                canceledRecurring.addAll(balanceServiceExternal.cancelAllRecurringByChargeAccount(tenantWrapper.getAccountId(), PROPERTY_PHASING_OUT));
            }
            log.info("Canceled recurring : {} ", canceledRecurring);
        }
    }

    private void cancelFutureCharges(PropertyPhasingOutWrapper propertyPhasingOutWrapper) throws GeneralException {
        log.info("start phase future Charges for : {}  ", propertyPhasingOutWrapper);
        propertyPhasingOutWrapper.setIsBill(false);
        propertyPhasingOutWrapper.setBusinessAction(PROPERTY_DEACTIVATION);
        balanceServiceExternal.cancelBillsByPropertyIdAndPhaseOutDate(propertyPhasingOutWrapper);
    }

    private void cancelFutureBills(PropertyPhasingOutWrapper propertyDetailsWithStatus) throws GeneralException {
        log.info("start phase future bills for : {}  ", propertyDetailsWithStatus);
        propertyDetailsWithStatus.setIsBill(true);
        propertyDetailsWithStatus.setBusinessAction(PROPERTY_DEACTIVATION);
        balanceServiceExternal.cancelBillsByPropertyIdAndPhaseOutDate(propertyDetailsWithStatus);

    }


}
