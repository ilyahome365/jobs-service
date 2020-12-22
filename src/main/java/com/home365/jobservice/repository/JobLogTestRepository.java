package com.home365.jobservice.repository;

import com.home365.jobservice.entities.JobLog;
import com.home365.jobservice.entities.JobLogTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobLogTestRepository extends JpaRepository<JobLogTest, Integer> {
}
