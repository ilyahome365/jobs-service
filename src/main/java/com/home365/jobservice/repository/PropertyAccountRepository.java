package com.home365.jobservice.repository;


import com.home365.jobservice.entities.PropertyAccountExtension;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyAccountRepository extends JpaRepository<PropertyAccountExtension, String> {
    List<PropertyAccountExtension> findByPropertyId(@Param("propertyId") String propertyId);

}
