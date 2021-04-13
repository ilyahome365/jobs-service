package com.home365.jobservice.entities.projection;

import lombok.Data;

public interface IOwnerRentNotification {
    String getContactId();

    String getPropertyId();

    String getFirstName();

    String getLastName();

    String getEmail();

    String getAddress();


}



