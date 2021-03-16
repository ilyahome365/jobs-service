package com.home365.jobservice.rest.impl;

import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.model.PropertyPhasingOutWrapper;
import com.home365.jobservice.rest.BalanceServiceFeign;
import com.home365.jobservice.rest.KeyCloakService;
import com.home365.jobservice.rest.KeycloakResponse;
import com.home365.jobservice.rest.PropertyPhaseOutExternal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PropertyPhaseOutExternalImpl implements PropertyPhaseOutExternal {
    private final KeyCloakService keyCloakService;
    private final BalanceServiceFeign balanceServiceFeign;

    public PropertyPhaseOutExternalImpl(KeyCloakService keyCloakService, BalanceServiceFeign balanceServiceFeign) {
        this.keyCloakService = keyCloakService;
        this.balanceServiceFeign = balanceServiceFeign;
    }

    @Override
    public List<Integer> cancelBillsByPropertyIdAndPhaseOutDate(PropertyPhasingOutWrapper propertyPhasingOutWrapper) throws GeneralException {
        KeycloakResponse token = keyCloakService.getKey();
        return balanceServiceFeign.cancelBillsByPropertyAndDueDate(token.getAccess_token(), propertyPhasingOutWrapper);
    }
}
