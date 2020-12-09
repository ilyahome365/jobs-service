package com.home365.jobservice.config;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.home365.jobservice.rest.GlobalErrorDecoder;
import com.home365.jobservice.rest.TenantFeignService;
import feign.Feign;
import feign.Logger;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfiguration {



    @Value("${service.tenant.url}")
    private String tenantService;


    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create();




    @Bean
    public TenantFeignService getTenantFeignService(){
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


}
