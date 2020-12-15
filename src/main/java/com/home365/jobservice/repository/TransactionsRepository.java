package com.home365.jobservice.repository;

import com.home365.jobservice.entities.Transactions;
import com.home365.jobservice.entities.TransactionsWithProjectedBalance;
import com.home365.jobservice.entities.projection.ILateFeeAdditionalInformationProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionsRepository extends JpaRepository<Transactions, String> {


    @Query(
            value = "SELECT * " +
                    "FROM Transactions " +
                    "WHERE TransactionType = 'Charge' " +
                    "      AND categoryName IN (:categoryNames)" +
                    "      AND Status IN (:status) " +
                    "AND TransactionId NOT IN ( " +
                    "   SELECT ReferenceTransactionId " +
                    "   FROM Transactions " +
                    "   WHERE BillType = 'lateFee' AND ReferenceTransactionId is not null" +
                    ")",
            nativeQuery = true)
    List<Transactions> findAllByBillTypeAndStatus(
            @Param("categoryNames") List<String> categoryNames,
            @Param("status") List<String> status
    );

    @Query(value = "select * from Transactions where RecurringTemplateId = :recurringTemplateId and status not in ('cancel') order by dueDate",
    nativeQuery = true)
    List<Transactions> findByRecurringTemplateId(@Param("recurringTemplateId") String recurringTemplateId);

    @Query(
            value = "SELECT distinct  p.New_name                          AS categoryName, " +
                    "                 na.Name                             AS accountingName," +
                    "                 New_primaryexpertiseExtensionBaseId AS categoryId, " +
                    "                 AccountingTypeId                    AS accountingTypeId " +
                    "FROM AccountingTypePrimaryexpertise a " +
                    "         INNER JOIN New_primaryexpertiseExtensionBase p on p.New_primaryexpertiseId=a.New_primaryexpertiseExtensionBaseId " +
                    "         INNER JOIN NewAccountingType na on na.Id=a.AccountingTypeId " +
                    "WHERE p.New_code = 11587"
            , nativeQuery = true)
    ILateFeeAdditionalInformationProjection getLateFeeAdditionalInformation();
}
