package com.home365.jobservice.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public abstract class DateAndTimeUtil {

    private DateAndTimeUtil(){

    }

    public static long getTimeDiff(Calendar currentCalendar, Date dueDate) {
        if (dueDate != null) {
            Calendar dueDateCalendar = Calendar.getInstance();
            dueDateCalendar.setTime(dueDate);
            long diffInMillies = Math.abs(currentCalendar.getTime().getTime() - dueDate.getTime());
            return TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        }
        return 0;
    }

    public static int getDaysLeft(LocalDate currentCalendar, LocalDate dueDate) {
        if (dueDate != null) {
            return (int) ChronoUnit.DAYS.between(currentCalendar, dueDate);
        }
        return 0;
    }

    public static Date convertFromLocalDateToDate(LocalDate localDate){
        return java.util.Date.from(localDate.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }
}
