package com.home365.jobservice.config;

import com.home365.jobservice.service.FindByIdAudit;
import com.home365.jobservice.service.impl.FindByAuditImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

@Configuration
public class BeanConfig {

    @Bean
    @Primary
    @Scope("prototype")
    public FindByIdAudit findByAudit(){
        return new FindByAuditImpl();
    }
}
