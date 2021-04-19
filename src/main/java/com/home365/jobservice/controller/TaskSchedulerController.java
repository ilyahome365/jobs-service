package com.home365.jobservice.controller;

import com.home365.jobservice.config.Constants;
import com.home365.jobservice.model.PropertyPhasingOutWrapper;
import com.home365.jobservice.model.TaskSchedulerResponse;
import com.home365.jobservice.tasks.PropertyPhaseOutTask;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@RestController
@Slf4j
@RequestMapping("/task-scheduler")
public class TaskSchedulerController {

    private final Scheduler scheduler;

    public TaskSchedulerController(Scheduler scheduler) {
        this.scheduler = scheduler;
    }


    @PostMapping("/property-deactivation")
    public ResponseEntity<TaskSchedulerResponse> propertySchedulerForPhasingOut(@RequestBody PropertyPhasingOutWrapper propertyPhasingOutWrapper) {
        try {
            LocalDate date = LocalDate.parse(propertyPhasingOutWrapper.getTriggerDateAndTime());
            if (date.isBefore(LocalDate.now())) {
                TaskSchedulerResponse scheduleEmailResponse = new TaskSchedulerResponse(false,
                        "dateTime must be after current time");
                return ResponseEntity.badRequest().body(scheduleEmailResponse);
            }
            JobDetail jobDetail = buildJobDetail(propertyPhasingOutWrapper);
            Trigger trigger = buildJobTrigger(jobDetail, date);
            Trigger existingTrigger = scheduler.getTrigger(new TriggerKey(propertyPhasingOutWrapper.getPropertyId(), Constants.PROPERTY_PHASING_OUT_TRIGGERS));
            if (existingTrigger != null) {
                scheduler.rescheduleJob(existingTrigger.getKey(), trigger);
            } else {
                scheduler.scheduleJob(jobDetail, trigger);
            }
            TaskSchedulerResponse scheduleEmailResponse = new TaskSchedulerResponse(true,
                    jobDetail.getKey().getName(), jobDetail.getKey().getGroup(), "Email Scheduled Successfully!");
            return ResponseEntity.ok(scheduleEmailResponse);
        } catch (SchedulerException ex) {
            log.error("Error scheduling email", ex);

            TaskSchedulerResponse scheduleEmailResponse = new TaskSchedulerResponse(false,
                    "Error scheduling email. Please try later!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(scheduleEmailResponse);
        }
    }


    private JobDetail buildJobDetail(PropertyPhasingOutWrapper propertyPhasingOutWrapper) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("propertyId", propertyPhasingOutWrapper.getPropertyId());
        return JobBuilder.newJob(PropertyPhaseOutTask.class)
                .withIdentity(propertyPhasingOutWrapper.getPropertyId(), Constants.EMAIL_JOBS)
                .withDescription("Send Email Job")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    private Trigger buildJobTrigger(JobDetail jobDetail, LocalDate startAt) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), Constants.EMAIL_TRIGGERS)
                .withDescription("Send Email Trigger")
                .startAt(Date.from(startAt.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }
}
