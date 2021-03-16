package com.home365.jobservice.flow;


import com.home365.jobservice.entities.PropertyExtension;
import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.model.PropertyPhasingOutWrapper;
import com.home365.jobservice.rest.PropertyPhaseOutExternal;
import com.home365.jobservice.service.PropertyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class PropertyPhasingOutFlow {

    private final PropertyPhaseOutExternal propertyPhaseOutExternal;
    private final PropertyService propertyService;

    public PropertyPhasingOutFlow(PropertyPhaseOutExternal propertyPhaseOutExternal, PropertyService propertyService) {
        this.propertyPhaseOutExternal = propertyPhaseOutExternal;
        this.propertyService = propertyService;
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
            propertyPhasingOutWrapper.setPropertyId(propertyId);
            propertyPhasingOutWrapper.setTriggerDateAndTime(property.get().getPhasingOutDate());
            cancelFutureBills(propertyPhasingOutWrapper);
        } catch (GeneralException e) {
            log.error(e.getMessage());
        }
    }

    private void cancelFutureBills(PropertyPhasingOutWrapper propertyDetailsWithStatus) throws GeneralException {
        log.info("start phase future bills for {}  ", propertyDetailsWithStatus);
        propertyPhaseOutExternal.cancelBillsByPropertyIdAndPhaseOutDate(propertyDetailsWithStatus);

    }
}
