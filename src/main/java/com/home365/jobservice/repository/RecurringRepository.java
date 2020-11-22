package com.home365.jobservice.repository;

import com.home365.jobservice.entities.Recurring;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecurringRepository extends JpaRepository<Recurring, String> {

    List<Recurring> findByActive(boolean isActive);
}
