package com.home365.jobservice.repository;

import com.home365.jobservice.entities.JobLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobLogRepository extends JpaRepository<JobLog, String> {
}
