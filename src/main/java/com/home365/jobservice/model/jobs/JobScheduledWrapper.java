package com.home365.jobservice.model.jobs;

import com.home365.jobservice.utils.LocationToTimeZoneConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;

import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobScheduledWrapper {
    private boolean active;
    private String name;
    private String cron;
    private String location;
    private TimeZone timeZone;
    private ScheduledFuture<?> scheduledFuture;
    private Runnable task;

    public JobScheduledWrapper(String name, String location, String cron, Runnable task) {
        this.name = name;
        this.location = location;
        this.cron = cron;
        this.task = task;
        this.timeZone = LocationToTimeZoneConverter.getTimeZone(location);
    }

    public void stopJob(boolean mayInterruptIfRunning) {
        log.info(String.format("Stop Job [%s] in Location [%s]", name, location));
        scheduledFuture.cancel(mayInterruptIfRunning);
        active = false;
    }

    public boolean isActive() {
        return scheduledFuture != null && !scheduledFuture.isCancelled() && active;
    }

    public boolean isNotActive() {
        return scheduledFuture.isCancelled() && !active;
    }

    public void setScheduledFuture(ScheduledFuture<?> scheduledFuture) {
        if (scheduledFuture != null) {
            this.scheduledFuture = scheduledFuture;
            this.active = true;
        }
    }

    public Trigger getTrigger() {
        return new CronTrigger(cron, timeZone);
    }

    public JobInfo getJobInfo() {
        JobInfo jobInfo = new JobInfo();
        jobInfo.setActive(isActive());
        jobInfo.setTaskName(getName());
        jobInfo.setCron(getCron());
        jobInfo.setTimeZone(getTimeZone());
        return jobInfo;
    }
}
