package com.home365.jobservice.repository;

import com.home365.jobservice.entities.Transactions;
import com.home365.jobservice.entities.projection.IDueDateEntry;
import com.home365.jobservice.entities.projection.ILateFeeAdditionalInformationProjection;
import com.home365.jobservice.entities.projection.IOwnerRentNotification;
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
                    "WHERE TransactionType = 'Charge' AND DueDate between '20210401 00:00:00' AND '20210401 23:59:59' AND pmAccountId = :pmAccountId and Amount > 0 " +
                    "      AND categoryName IN (:categoryNames)" +
                    "      AND Status IN (:status) " +
                    "AND TransactionId NOT IN ( " +
                    "   SELECT ReferenceTransactionId " +
                    "   FROM Transactions " +
                    "   WHERE BillType = 'lateFee' AND ReferenceTransactionId is not null" +
                    ")",
            nativeQuery = true)
    List<Transactions> findAllByBillTypeAndStatus(@Param("categoryNames") List<String> categoryNames, @Param("status") List<String> status, @Param("pmAccountId") String pmAccountId);

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
                    "WHERE na.Name = 'Late Fee Income'"
            , nativeQuery = true)
    ILateFeeAdditionalInformationProjection getLateFeeAdditionalInformation();

    @Query(value = "select * from Transactions where propertyId = :propertyId and " +
            "CategoryName = 'Tenant Rent' and status not in ('paid', 'cancel') ", nativeQuery = true)
    List<Transactions> findTenantRentTransactionsByPropertyId(@Param("propertyId") String propertyId);

    @Query(value = "select ChargeAccountId, max(DueDate) MaxDueDate, caeb.new_contactid ContactId, c.FirstName, c.LastName, c.EMailAddress1, tn.tenantJson  from Transactions tr\n" +
            "                inner join New_contactaccountExtensionBase caeb on caeb.new_accountid = tr.ChargeAccountId\n" +
            "                inner join Contact c on c.ContactId = caeb.new_contactid\n" +
            "                inner join Tenant tn on tn.propertyId = tr.PropertyId\n" +
            "                where ChargeAccountId in (select a.AccountId from Contact c\n" +
            "                         inner join New_contactaccountExtensionBase ca on ca.new_contactid = c.ContactId\n" +
            "                         inner join dbo.New_contactaccountBase cab on cab.New_contactaccountId=ca.New_contactaccountId\n" +
            "                         inner join dbo.AccountExtensionBase a on a.AccountId=ca.New_AccountId\n" +
            "                where cab.statuscode=1 and a.New_status in(1,4,6) and a.New_BusinessType = 10)\n" +
            "                and status in ('readyForPayment') and tr.PmAccountId = :pmAccountId\n" +
            "                group by ChargeAccountId, caeb.new_contactid, c.FirstName, c.LastName, c.EMailAddress1, tn.tenantJson", nativeQuery = true)
    List<IDueDateEntry> getDueDateNotificationsByPmAccountId(@Param("pmAccountId") String pmAccountId);

    @Query(value = "select ContactId,PropertyId,\n" +
            "       c.FirstName, c.LastName, c.EMailAddress1 email,N'• '+isnull(tn.New_ShortenAddress ,'' )+isnull( ' ,'+tn.New_Unit ,'')\n" +
            "                                                     + isnull(' ,'+tn.New_Building ,'') +isnull(' ,'+tn.city ,'') address" +
            "  from Transactions tr\n" +
            "                            inner join New_contactaccountExtensionBase caeb on caeb.new_accountid = tr.ReceiveAccountId\n" +
            "                            inner join Contact c on c.ContactId = caeb.new_contactid\n" +
            "                            inner join New_propertyExtensionBase tn on tn.New_propertyId = tr.PropertyId\n" +
            "                            where ReceiveAccountId in (select a.AccountId from Contact c\n" +
            "                                     inner join New_contactaccountExtensionBase ca on ca.new_contactid = c.ContactId\n" +
            "                                   inner join dbo.New_contactaccountBase cab on cab.New_contactaccountId=ca.New_contactaccountId\n" +
            "                                   inner join dbo.AccountExtensionBase a on a.AccountId=ca.New_AccountId\n" +
            "                    where cab.statuscode=1 and a.New_status in(1,4,6) and a.New_BusinessType =8)\n" +
            "                           and status in ('readyForPayment','paymentFailed','overDue') and tr.CategoryName = 'Tenant Rent' \n" +
            "                              and tr.PmAccountId =:pmAccountId and tr.DueDate between :firstDate AND :lastDate\n" +
            "                            group by ContactId,PropertyId, caeb.new_contactid, c.FirstName, c.LastName, c.EMailAddress1,tn.New_ShortenAddress,tn.New_Unit,tn.New_Building,tn.city", nativeQuery = true)
    List<IOwnerRentNotification> getOwnerRentNotification(@Param("pmAccountId") String pmAccountId, String firstDate, String lastDate);
}
