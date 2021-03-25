package com.home365.jobservice.config;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.home365.jobservice.rest.BalanceServiceFeign;
import com.home365.jobservice.rest.GlobalErrorDecoder;
import com.home365.jobservice.rest.KeycloakFeignService;
import com.home365.jobservice.rest.TenantFeignService;
import feign.Feign;
import feign.Logger;
import feign.form.FormEncoder;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class FeignConfiguration {


    @Value("${service.tenant.url}")
    private String tenantService;

    @Value("${keyCloack.url}")
    private String keycloakUrl;

    @Value("${service.balance.url}")
    private String balanceService;


    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, type, jsonDeserializationContext) -> LocalDateTime.parse(json.getAsJsonPrimitive().getAsString()))
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, type, jsonDeserializationContext) -> LocalDate.parse(json.getAsJsonPrimitive().getAsString()))
            .create();



    @Bean
    public TenantFeignService getTenantFeignService() {
        return Feign
                .builder()
                .client(new OkHttpClient())
                .encoder(new GsonEncoder(gson))
                .decoder(new GsonDecoder(gson))
                .logger(new Slf4jLogger(TenantFeignService.class))
                .logLevel(Logger.Level.FULL)
                .errorDecoder(new GlobalErrorDecoder())
                .target(TenantFeignService.class, tenantService);
    }

    @Bean
    public BalanceServiceFeign getBalanceFeignService() {
        return Feign
                .builder()
                .client(new OkHttpClient())
                .encoder(new GsonEncoder(gson))
                .decoder(new GsonDecoder(gson))
                .logger(new Slf4jLogger(BalanceServiceFeign.class))
                .logLevel(Logger.Level.FULL)
                .errorDecoder(new GlobalErrorDecoder())
                .target(BalanceServiceFeign.class, balanceService);
    }


    @Bean
    public KeycloakFeignService getKeycloackFeignService() {
        return Feign
                .builder()
                .client(new OkHttpClient())
                .encoder(new FormEncoder())
                .decoder(new GsonDecoder(gson))
                .logger(new Slf4jLogger(KeycloakFeignService.class))
                .logLevel(Logger.Level.FULL)
                .target(KeycloakFeignService.class, keycloakUrl);
    }
}
