package com.home365.jobservice.repository;

import com.home365.jobservice.entities.TransactionsWithProjectedBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionsWithProjectedBalanceRepo extends JpaRepository<TransactionsWithProjectedBalance, String> {
    @Query(
            value = "select (select SUM(projected_balance) as projected_balance " +
                    "        from PROPERTY_BALANCE " +
                    "        where account_id = t.ChargeAccountId) " +
                    "           projected_balance, t.*" +
                    "FROM Transactions t " +
                    "         inner join [dbo].[AccountExtensionBase] c_ace on t.ChargeAccountId = c_ace.AccountId " +
                    "where c_ace.New_BusinessType = 8 " +
                    "  and t.Status = 'pendingDue' " +
                    "  and convert(date, DueDate) <= :cycleDate"+"  and t.PmAccountId =:pmAccountId",
            nativeQuery = true)
    List<TransactionsWithProjectedBalance> getTransactionsWithProjectedBalance(@Param("cycleDate") String cycleDate,@Param("pmAccountId") String pmAccountId);
}
