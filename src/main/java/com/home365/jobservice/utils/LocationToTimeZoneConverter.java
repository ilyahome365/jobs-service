package com.home365.jobservice.utils;

import java.util.TimeZone;

public abstract class LocationToTimeZoneConverter {
    public static TimeZone getTimeZone(String location){
        return TimeZone.getDefault();
    }
}
