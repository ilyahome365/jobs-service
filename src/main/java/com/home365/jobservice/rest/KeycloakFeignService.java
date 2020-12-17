package com.home365.jobservice.rest;


import com.home365.jobservice.exception.GeneralException;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface KeycloakFeignService {
    @RequestLine("POST /token")
    @Headers({"Content-Type: application/x-www-form-urlencoded; charset=UTF-8"})
    KeycloakResponse getKey(@Param("client_id") String clientId,
                                                             @Param("username") String username,
                                                             @Param("password") String password,
                                                             @Param("grant_type") String grantType) throws GeneralException;
}
