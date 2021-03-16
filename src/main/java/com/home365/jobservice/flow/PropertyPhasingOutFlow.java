package com.home365.jobservice.flow;


import com.home365.jobservice.entities.PropertyExtension;
import com.home365.jobservice.entities.enums.PropertyStatus;
import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.model.PropertyPhasingOutWrapper;
import com.home365.jobservice.model.enums.TenantStatus;
import com.home365.jobservice.model.wrapper.TenantWrapper;
import com.home365.jobservice.rest.PropertyPhaseOutExternal;
import com.home365.jobservice.rest.TenantServiceExternal;
import com.home365.jobservice.service.PropertyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PropertyPhasingOutFlow {

    private final PropertyPhaseOutExternal propertyPhaseOutExternal;
    private final PropertyService propertyService;
    private final TenantServiceExternal tenantServiceExternal;

    public PropertyPhasingOutFlow(PropertyPhaseOutExternal propertyPhaseOutExternal, PropertyService propertyService, TenantServiceExternal tenantServiceExternal) {
        this.propertyPhaseOutExternal = propertyPhaseOutExternal;
        this.propertyService = propertyService;
        this.tenantServiceExternal = tenantServiceExternal;
    }

    public void startPropertyPhasingOut(String propertyId) {
        log.info("Start property phasing out");
        try {
            PropertyPhasingOutWrapper propertyPhasingOutWrapper = new PropertyPhasingOutWrapper();
            Optional<PropertyExtension> property = propertyService.findPropertyById(propertyId);
            if (property.isEmpty()) {
                GeneralException generalException = new GeneralException();
                generalException.setMessage("No property for " + propertyId);
                throw generalException;
            }
            if(!property.get().getPropertyStatus().equalsIgnoreCase(PropertyStatus.phasingOut.name())){
                GeneralException generalException = new GeneralException();
                generalException.setMessage("property is not on property phase out");
            }

            propertyPhasingOutWrapper.setPropertyId(propertyId);
            propertyPhasingOutWrapper.setTriggerDateAndTime(property.get().getPhasingOutDate().toString());
            cancelFutureBills(propertyPhasingOutWrapper);
            cancelFutureCharges(propertyPhasingOutWrapper);
            cancelRecurringForProperty(propertyPhasingOutWrapper);
        } catch (GeneralException e) {
            log.error(e.getMessage());
        }
    }

    private void cancelRecurringForProperty(PropertyPhasingOutWrapper propertyPhasingOutWrapper) throws GeneralException {
        log.info("start cancel recurring charges for : {} ", propertyPhasingOutWrapper);
        List<TenantWrapper> tenants = tenantServiceExternal.getTenantsByPropertyId(propertyPhasingOutWrapper.getPropertyId());
        List<String> canceledRecurring = new ArrayList<>();
        if (!CollectionUtils.isEmpty(tenants)) {
            List<TenantWrapper> activeTenants = tenants.stream().filter(tenantWrapper -> !tenantWrapper.getTenantStatus().equals(TenantStatus.Inactive)).collect(Collectors.toList());
            for (TenantWrapper tenantWrapper : activeTenants) {
                canceledRecurring.addAll(propertyPhaseOutExternal.cancelAllRecurringByChargeAccount(tenantWrapper.getAccountId()));
            }
            log.info("Canceled recurring : {} ", canceledRecurring);
        }
    }

    private void cancelFutureCharges(PropertyPhasingOutWrapper propertyPhasingOutWrapper) throws GeneralException {
        log.info("start phase future Charges for : {}  ", propertyPhasingOutWrapper);
        propertyPhasingOutWrapper.setIsBill(false);
        propertyPhaseOutExternal.cancelBillsByPropertyIdAndPhaseOutDate(propertyPhasingOutWrapper);
    }

    private void cancelFutureBills(PropertyPhasingOutWrapper propertyDetailsWithStatus) throws GeneralException {
        log.info("start phase future bills for : {}  ", propertyDetailsWithStatus);
        propertyDetailsWithStatus.setIsBill(true);
        propertyPhaseOutExternal.cancelBillsByPropertyIdAndPhaseOutDate(propertyDetailsWithStatus);

    }
}
