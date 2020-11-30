package com.home365.jobservice.repository;

import com.home365.jobservice.entities.LateFeeAdditionalInformationProjection;
import com.home365.jobservice.entities.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TransactionsRepository extends JpaRepository<Transactions, String> {
    @Query(
            value = "select (select SUM(projected_balance) as projected_balance " +
                    "        from PROPERTY_BALANCE " +
                    "        where account_id = t.ChargeAccountId) " +
                    "           projected_balance, t.*" +
                    "FROM Transactions t " +
                    "         inner join [dbo].[AccountExtensionBase] c_ace on t.ChargeAccountId = c_ace.AccountId " +
                    "where c_ace.New_BusinessType = 8 " +
                    "  and t.Status = 'pendingDue' " +
                    "  and convert(date, DueDate) <= :cycleDate",
            nativeQuery = true)
    List<Transactions> getTransactionsWithProjectedBalance(@Param("cycleDate") String cycleDate);

    @Query(
            value = "SELECT * " +
                    "FROM Transactions " +
                    "WHERE TransactionType = 'Charge' " +
                    "      AND categoryName IN (:categoryNames)" +
                    "      AND Status IN (:status) " +
                    "      AND DueDate < :dueDate " +
                    "AND TransactionId NOT IN ( " +
                    "   SELECT ReferenceTransactionId " +
                    "   FROM Transactions " +
                    "   WHERE BillType = 'lateFee' AND ReferenceTransactionId is not null" +
                    ")",
            nativeQuery = true)
    List<Transactions> findAllByBillTypeAndStatusAndDueDateBefore(
            @Param("categoryNames") List<String> categoryNames,
            @Param("status") List<String> status,
            @Param("dueDate") Date dueDate
    );

    List<Transactions> findByRecurringTemplateId(String recurringTemplateId);

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
    LateFeeAdditionalInformationProjection getLateFeeAdditionalInformation();
}
