package com.home365.jobservice.rest.model.enums;

import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public enum ApplicationStatus {
    interested, inScreening, decisionPending, savedAsCandidate, rejected, startedLeasingProcess, LeasingProcessCompleted, canceled;

    public static Optional<ApplicationStatus> getByValue(String value) {
        Optional<ApplicationStatus> result = Optional.empty();
        if (!ObjectUtils.isEmpty(value)) {
            String val = value.replaceAll("\\s+", "");
            result = Arrays.stream(ApplicationStatus
                    .values())
                    .filter(name -> name.name().equalsIgnoreCase(val))
                    .findAny();
        }
        return result;
    }

    public String getBusinessName() {
        String[] strings = org.apache.commons.lang3.StringUtils.splitByCharacterTypeCamelCase(this.name());
        List<String> upperCase = new LinkedList<>();
        for (String string : strings) {
            upperCase.add(org.apache.commons.lang3.StringUtils.capitalize(string));
        }
        return String.join(" ", upperCase);
    }
}
