package com.home365.jobservice.rest.impl;

import com.home365.jobservice.config.AppPropertiesExternal;
import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.rest.KeyCloakService;
import com.home365.jobservice.rest.KeycloakFeignService;
import com.home365.jobservice.rest.KeycloakResponse;
import org.springframework.stereotype.Service;

@Service
public class KeyCloakServiceImpl implements KeyCloakService {
    private final KeycloakFeignService keycloakFeignService;
    private final AppPropertiesExternal appProperties;
    public KeyCloakServiceImpl(KeycloakFeignService keycloakFeignService, AppPropertiesExternal appProperties) {
        this.keycloakFeignService = keycloakFeignService;
        this.appProperties = appProperties;
    }

    @Override
    public KeycloakResponse getKey() throws GeneralException {
        return keycloakFeignService.getKey(
                appProperties.getClientId(),
                appProperties.getUsername(),
                appProperties.getPassword(),
                appProperties.getGrantType()
        );
    }
}
