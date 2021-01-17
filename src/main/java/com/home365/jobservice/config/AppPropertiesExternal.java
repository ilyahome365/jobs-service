package com.home365.jobservice.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class AppPropertiesExternal {

    @Value("${keyCloack.clientId}")
    private String clientId;

    @Value("${keyCloack.username}")
    private String username;

    @Value("${keyCloack.password}")
    private String password;

    @Value("${keyCloack.grantType}")
    private String grantType;
}
