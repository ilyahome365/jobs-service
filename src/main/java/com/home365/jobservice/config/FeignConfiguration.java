package com.home365.jobservice.config;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.home365.jobservice.rest.*;
import feign.Feign;
import feign.Logger;
import feign.Request;
import feign.form.FormEncoder;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
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

    @Value("${service.applicant.url}")
    private String applicantService;
    private final Gson gson;
    ObjectMapper objectMapper = new ObjectMapper();

    private Integer read = 1800000;
    private Integer connect = 1800000;


    public FeignConfiguration() {
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, type, jsonDeserializationContext) -> LocalDateTime.parse(json.getAsJsonPrimitive().getAsString()))
                .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, type, jsonDeserializationContext) -> LocalDate.parse(json.getAsJsonPrimitive().getAsString()))
                .create();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    @Bean
    public TenantFeignService getTenantFeignService() {
        return Feign
                .builder()
                .client(new OkHttpClient())
                .encoder(new JacksonEncoder(objectMapper))
                .decoder(new JacksonDecoder(objectMapper))
                .logger(new Slf4jLogger(TenantFeignService.class))
                .logLevel(Logger.Level.FULL)
                .errorDecoder(new GlobalErrorDecoder())
                .target(TenantFeignService.class, tenantService);
    }

    @Bean
    public BalanceServiceFeign getBalanceFeignService() {
        return Feign
                .builder()
                .options(new Request.Options(connect, read))
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

    @Bean
    public ApplicantFeignService getApplicantFeignService() {
        return Feign
                .builder()
                .client(new OkHttpClient())
                .encoder(new JacksonEncoder(objectMapper))
                .decoder(new JacksonDecoder(objectMapper))
                .logger(new Slf4jLogger(ApplicantFeignService.class))
                .logLevel(Logger.Level.FULL)
                .errorDecoder(new GlobalErrorDecoder())
                .target(ApplicantFeignService.class,applicantService );
    }


}
