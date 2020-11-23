package com.home365.jobservice.repository;

import com.home365.jobservice.entities.JobConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobsConfigurationRepository extends JpaRepository<JobConfiguration, Long> {
}
