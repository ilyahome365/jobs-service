package com.home365.jobservice.utils;


import org.springframework.util.StringUtils;

public class AddressUtils {

    public static String addUnitAndBuildingToAddress(String address, String unit, String building) {
        String returnAddress = address;

        if (!StringUtils.isEmpty(unit) || !StringUtils.isEmpty(building)) {

            if (!StringUtils.isEmpty(unit)) {
                returnAddress += String.format(", Unit %s", unit);
                if (!StringUtils.isEmpty(building)) {
                    returnAddress += String.format(" Bldg %s", building);
                }
            } else if (!StringUtils.isEmpty(building)) {
                returnAddress += String.format(", Bldg %s", building);
            }
        }
        return returnAddress;
    }
}
