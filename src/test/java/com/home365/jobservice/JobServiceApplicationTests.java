package com.home365.jobservice;

import com.home365.jobservice.rest.KeyCloakService;
import com.home365.jobservice.rest.TenantFeignService;
import com.home365.jobservice.service.JobLogService;
import com.home365.jobservice.service.TransactionsService;
import com.home365.jobservice.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Slf4j
@ActiveProfiles("prod")
class JobServiceApplicationTests {
    private final String JOB_PENDING_DUE = "jobPendingJobTest";
    @Autowired
    JobLogService jobLogService;
    @Autowired
    LateFeeJobServiceImpl lateFeeJobService;
    @Autowired
    DueDateNotificationServiceImpl dueDateNotificationService;
    @Autowired
    LeaseUpdatingServiceImpl leaseUpdatingService;
    @Autowired
    private TransactionsService transactionsService;
    @Autowired
    RecurringServiceImpl recurringService;
    @Autowired
    TenantFeignService tenantServiceExternal;
    @Autowired
    KeyCloakService keyCloakService;
    @Autowired
    PhaseOutPropertyServiceImpl phaseOutPropertyService;

    @Test
    void contextLoads() {
/*        Transactions transactions = new Transactions();
        transactions.setPropertyId("1");
        transactions.setCategoryName("test");
        Transactions save = transactionsService.save(transactions);*/
    }


/*    @Test
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
    }*/

    @Test
    public void lateFeeLVTest() throws Exception {
        lateFeeJobService.execute("F90E128A-CD00-4DF7-B0D0-0F40F80D623A");
    }

    @Test
    public void lateFeeATTest() throws Exception {
        lateFeeJobService.execute("F90E128A-CD00-4DF7-B0D0-0F40F80D624A");
    }

    @Test
    public void dueDateNotificationTest() throws Exception {
        dueDateNotificationService.sendNotificationForDueDateTenants("F90E128A-CD00-4DF7-B0D0-0F40F80D623A");
    }

    @Test
    public void leaseUpdateTest() throws Exception {
        leaseUpdatingService.execute("F90E128A-CD00-4DF7-B0D0-0F40F80D623A");
    }

    @Test
    public void recurringChargesTestLV() throws Exception {
        recurringService.createTransactionsForRecurringCharges("F90E128A-CD00-4DF7-B0D0-0F40F80D623A");
    }

    @Test
    public void recurringChargesTestAT() throws Exception {
        recurringService.createTransactionsForRecurringCharges("F90E128A-CD00-4DF7-B0D0-0F40F80D624A");
    }

    @Test
    public void phaseOutProperty() {
        phaseOutPropertyService.executeJob("F90E128A-CD00-4DF7-B0D0-0F40F80D623A");

    }


}
