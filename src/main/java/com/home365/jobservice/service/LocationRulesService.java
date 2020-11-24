package com.home365.jobservice.service;

import com.home365.jobservice.entities.LocationRules;

import java.util.Optional;

public interface LocationRulesService {
    Optional<LocationRules> findLocationRulesById(String id);
}
