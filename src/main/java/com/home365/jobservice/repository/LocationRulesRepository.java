package com.home365.jobservice.repository;

import com.home365.jobservice.entities.LocationRules;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocationRulesRepository extends JpaRepository<LocationRules, String> {

    Optional<LocationRules> findByPmAccountId(String pmAccountId);
}
