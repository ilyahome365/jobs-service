package com.home365.jobservice;

import com.home365.jobservice.rest.KeyCloakService;
import com.home365.jobservice.rest.TenantFeignService;
import com.home365.jobservice.service.JobLogService;
import com.home365.jobservice.service.OwnerNotificationsService;
import com.home365.jobservice.service.TransactionsService;
import com.home365.jobservice.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Slf4j
@ActiveProfiles("test")
class JobServiceApplicationTests {
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

    @Autowired
    OwnerNotificationsService ownerNotificationsService;

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

    @Test void ownerNotificationTest(){
        ownerNotificationsService.createOwnerNotification("F90E128A-CD00-4DF7-B0D0-0F40F80D623A");
    }
}
