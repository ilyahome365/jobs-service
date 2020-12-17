package com.home365.jobservice.rest;


import com.home365.jobservice.exception.GeneralException;

public interface KeyCloakService {
KeycloakResponse getKey() throws GeneralException;
}
