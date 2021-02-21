package com.home365.jobservice.repository;

import com.home365.jobservice.entities.AccountExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<AccountExtension, String> {

}
