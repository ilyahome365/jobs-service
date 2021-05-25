package com.home365.jobservice.service.impl;

import com.home365.jobservice.entities.LocationRules;
import com.home365.jobservice.repository.LocationRulesRepository;
import com.home365.jobservice.service.LocationRulesService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LocationRulesServiceImpl implements LocationRulesService {

    private final LocationRulesRepository locationRulesRepository;

    public LocationRulesServiceImpl(LocationRulesRepository locationRulesRepository) {
        this.locationRulesRepository = locationRulesRepository;
    }


    @Override
    public Optional<LocationRules> findLocationRulesById(String id) {
        return locationRulesRepository.findById(id);
    }

    @Override
    public Optional<LocationRules> findByPmAccountId(String pmAccountId) {
        return  locationRulesRepository.findByPmAccountId(pmAccountId);
    }
}
