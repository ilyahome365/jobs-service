package com.home365.jobservice.utils;

import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public abstract class DateAndTimeUtil {

    //TODO: switch to sql calculation when using db to region
    public static long getTimeDiff(Calendar currentCalendar, Date dueDate) {
        if (dueDate != null) {
            Calendar dueDateCalendar = Calendar.getInstance();
            dueDateCalendar.setTime(dueDate);
            long diffInMillies = Math.abs(currentCalendar.getTime().getTime() - dueDate.getTime());
            return TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        }
        return 0;
    }

    public static int getDaysLeft(Calendar currentCalendar, Date dueDate) {
        if (dueDate != null) {
            return (int) ChronoUnit.DAYS.between(currentCalendar.toInstant(), dueDate.toInstant());
        }
        return 0;
    }

    public static Date addMonths(int amount, Date endDate) {
        if (endDate != null) {
            Calendar dueDateCalendar = Calendar.getInstance();
            dueDateCalendar.setTime(endDate);
            dueDateCalendar.add(Calendar.MONTH, amount);
            return dueDateCalendar.getTime();
        }
        return new Date();
    }
}
