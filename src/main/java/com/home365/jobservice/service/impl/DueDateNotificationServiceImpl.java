package com.home365.jobservice.service.impl;

import com.home365.jobservice.model.JobExecutionResults;
import com.home365.jobservice.service.DueDateNotificationService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DueDateNotificationServiceImpl implements DueDateNotificationService {
    private final JdbcTemplate jdbcTemplate;

    public DueDateNotificationServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

    }

    @Override
    public JobExecutionResults sendNotificationForDueDateTenants() {
        String query = "select ChargeAccountId, max(DueDate) MaxDueDate, caeb.new_contactid, c.FullName, c.EMailAddress1  from Transactions tr\n" +
                "inner join New_contactaccountExtensionBase caeb on caeb.new_accountid = tr.ChargeAccountId\n" +
                "inner join Contact c on c.ContactId = caeb.new_contactid\n" +
                "where ChargeAccountId in (\n" +
                "select a.AccountId\n" +
                "from Contact c\n" +
                "         inner join New_contactaccountExtensionBase ca on ca.new_contactid = c.ContactId\n" +
                "         inner join dbo.New_contactaccountBase cab on cab.New_contactaccountId=ca.New_contactaccountId\n" +
                "         inner join dbo.AccountExtensionBase a on a.AccountId=ca.New_AccountId\n" +
                "where cab.statuscode=1 and a.New_status in(1,4,6)\n" +
                "    and a.New_BusinessType = 10\n" +
                ") and status in ('readyForPayment') group by ChargeAccountId, caeb.new_contactid, c.FullName, c.EMailAddress1";

        List<Map<String, Object>> tenantChargesList = jdbcTemplate.queryForList(query);

        tenantChargesList.forEach(entry -> sendDueDateNotification((String) entry.get("MaxDueDate"), (String) entry.get("FullName"), (String) entry.get("EMailAddress1")));

        return JobExecutionResults.builder().build();
    }

    private void sendDueDateNotification(String maxDueDate, String fullName, String eMailAddress1) {
    }
}
