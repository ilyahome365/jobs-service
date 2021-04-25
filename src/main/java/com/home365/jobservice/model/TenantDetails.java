package com.home365.jobservice.model;

import com.home365.jobservice.model.enums.TenantType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantDetails {

    private String accountId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String contactId;
    private TenantType type;
    private Boolean contactPerson;


    public void setFirstName(String firstName) {
        this.firstName = firstName.trim();
    }

    public void setLastName(String lastName) {
        this.lastName = lastName.trim();
    }

    public String formattedString() {
        return
                " firstName='" + firstName + '\'' +
                        " lastName='" + lastName + '\'' +
                        " email='" + email + '\'' +
                        " phoneNumber='" + phoneNumber + '\'' +
                        " type=" + type +
                        " contactPerson=" + contactPerson
                ;
    }

    @Override
    public String toString() {
        return
                "accountId='" + accountId + '\'' +
                        ", firstName='" + firstName + '\'' +
                        ", lastName='" + lastName + '\'' +
                        ", email='" + email + '\'' +
                        ", phoneNumber='" + phoneNumber + '\'' +
                        ", contactId='" + contactId + '\'' +
                        ", type=" + type +
                        ", contactPerson=" + contactPerson
                ;
    }
}
