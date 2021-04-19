package com.home365.jobservice.utils;

import java.util.TimeZone;

public abstract class LocationToTimeZoneConverter {

    private LocationToTimeZoneConverter(){

    }
    public static TimeZone getTimeZone(){
        return TimeZone.getDefault();
    }
}
