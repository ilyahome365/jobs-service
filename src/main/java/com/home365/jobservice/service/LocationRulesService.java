package com.home365.jobservice.service;

import com.home365.jobservice.entities.LocationRules;

import java.util.List;
import java.util.Optional;

public interface LocationRulesService {
    Optional<LocationRules> findLocationRulesById(String id);

    Optional<LocationRules> findByPmAccountId(String pmAccountId);
}
