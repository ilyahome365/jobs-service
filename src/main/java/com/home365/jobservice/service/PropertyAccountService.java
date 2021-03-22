package com.home365.jobservice.service;

import com.home365.jobservice.entities.PropertyAccountExtension;

import java.util.List;

public interface PropertyAccountService {
    List<PropertyAccountExtension> getByPropertyId(String propertyId);
}
