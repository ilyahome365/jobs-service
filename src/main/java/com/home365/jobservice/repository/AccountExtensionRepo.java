package com.home365.jobservice.repository;

import com.home365.jobservice.entities.AccountExtensionBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountExtensionRepo extends JpaRepository<AccountExtensionBase, String> {

    @Query(
            value = "select AB.*\n" +
                    "from New_contactaccountExtensionBase ca\n" +
                    "    inner join New_contactaccountBase c on ca.New_contactaccountId = c.New_contactaccountId and statuscode = 1\n" +
                    "    inner join AccountExtensionBase AB on ca.new_accountid = AB.AccountId\n" +
                    "where ca.new_contactid = :contactId and AB.New_BusinessType = :businessType" +
                    " and c.statuscode=1 and AB.New_status in(1,4,6)",
            nativeQuery = true)
    List<AccountExtensionBase> findAccountsByContactIdAndBusinessType(@Param("contactId") String contactId, @Param("businessType") int businessType);

    Optional<AccountExtensionBase> findDistinctByAccountTypeAndNewManagerId(@Param("accountType") String transferTo, @Param("newManagerId") String newManagerId);

    Optional<AccountExtensionBase> findStripeAccountByAccountId(@Param("accountId") String accountId);


}
