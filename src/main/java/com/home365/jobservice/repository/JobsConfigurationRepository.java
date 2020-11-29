package com.home365.jobservice.repository;

import com.home365.jobservice.entities.JobConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobsConfigurationRepository extends JpaRepository<JobConfiguration, Long> {
    Optional<JobConfiguration> findByLocationAndName(String location, String name);
}
