package com.home365.jobservice.repository;

import com.home365.jobservice.entities.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentsRepo extends JpaRepository<Payments, String> {
}
