package com.home365.jobservice.repository;


import com.home365.jobservice.entities.PropertyAccountExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyAccountRepository extends JpaRepository<PropertyAccountExtension, String> {


}
