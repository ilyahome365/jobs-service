package com.home365.jobservice;

import com.home365.jobservice.entities.JobLog;
import com.home365.jobservice.model.PendingStatusJobData;
import com.home365.jobservice.service.JobLogService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;

@SpringBootTest
@Slf4j
@ActiveProfiles("prod")
class JobServiceApplicationTests {
    private final String JOB_PENDING_DUE ="jobPendingJobTest";
    @Autowired
    JobLogService jobLogService;
    @Test
    void contextLoads() {
    }


    @Test
    public void testJobLog(){
        PendingStatusJobData pendingStatusJobData = new PendingStatusJobData();
        pendingStatusJobData.setPendingContribution(1L);
        pendingStatusJobData.setReadyForPayment(1L);
        createJobLog(pendingStatusJobData, "TEST");
    }

    private void createJobLog(PendingStatusJobData pendingStatusJobData, String cycleDate) {
        log.info("create job log with ready for payment : {}  , pendingContribution: {} , with cycle date : {} "
                , pendingStatusJobData.getReadyForPayment(), pendingStatusJobData.getPendingContribution(), cycleDate);
        LocalDate localDate = LocalDate.now();

        Date date = new Date();
        Timestamp currentTimeAndDate = new Timestamp(date.getTime());
        JobLog jobLog = new JobLog();
//        jobLog.setId(UUID.randomUUID().toString());
        jobLog.setJobName(JOB_PENDING_DUE);
        jobLog.setLastRun(localDate);
        jobLog.setDate(currentTimeAndDate);
        String jobComment = "job " + JOB_PENDING_DUE + " run - readyForPayment : "
                + pendingStatusJobData.getReadyForPayment() + " , pendingContribution : " + pendingStatusJobData.getPendingContribution() + ", with cycle date : " + cycleDate;
        jobLog.setComments(jobComment);
        jobLogService.saveJobLog(jobLog);
    }

}
