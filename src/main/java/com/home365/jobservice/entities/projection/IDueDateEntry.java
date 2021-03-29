package com.home365.jobservice.entities.projection;

import java.sql.Timestamp;

public interface IDueDateEntry {

    String getChargeAccountId();

    Timestamp getMaxDueDate();

    String getContactId();

    String getFirstName();

    String getLastName();

    String getEMailAddress1();

    String getTenantJson();
}
