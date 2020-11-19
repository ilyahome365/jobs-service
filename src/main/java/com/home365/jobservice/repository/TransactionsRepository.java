package com.home365.jobservice.repository;

import com.home365.jobservice.entities.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionsRepository extends JpaRepository<Transactions, String> {
    @Query(
            value = "select (select SUM(projected_balance) as projected_balance\n" +
                    "        from PROPERTY_BALANCE\n" +
                    "        where account_id = t.ChargeAccountId)\n" +
                    "           projected_balance, t.*" +
                    "FROM Transactions t \n" +
                    "         inner join [dbo].[AccountExtensionBase] c_ace on t.ChargeAccountId = c_ace.AccountId\n" +
                    "where c_ace.New_BusinessType = 8\n" +
                    "  and t.Status = 'pendingDue'\n" +
                    "  and convert(date, DueDate) <= :cycleDate",
            nativeQuery = true)
    List<Transactions> getTransactionsWithProjectedBalance(@Param("cycleDate") String cycleDate);


}
