package com.flashsale.backend.service;

import com.flashsale.backend.common.ResultCode;
import com.flashsale.backend.dto.response.JobResponse;
import com.flashsale.backend.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @description JobServiceTest(By using mock not for db)
 * @author Yang-Hsu
 * @date 2026/7/17
 */
@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private Scheduler scheduler;

    @InjectMocks
    private JobServiceImpl jobService;

    @Test
    @DisplayName("查詢所有 Job - 有 Trigger 回傳完整狀態")
    void getAllJobs_jobWithTrigger_returnsJobResponse() throws SchedulerException {
        JobKey jobKey = JobKey.jobKey("sampleJob", "DEFAULT");
        JobDetail jobDetail = mock(JobDetail.class);
        when(jobDetail.getDescription()).thenReturn("sample job description");

        CronTrigger trigger = mock(CronTrigger.class);
        TriggerKey triggerKey = TriggerKey.triggerKey("sampleJobTrigger", "DEFAULT");
        when(trigger.getKey()).thenReturn(triggerKey);
        when(trigger.getCronExpression()).thenReturn("0 0 0 * * ?");
        when(trigger.getPreviousFireTime()).thenReturn(new Date());
        when(trigger.getNextFireTime()).thenReturn(new Date());

        when(scheduler.getJobKeys(any(GroupMatcher.class))).thenReturn(Set.of(jobKey));
        when(scheduler.getJobDetail(jobKey)).thenReturn(jobDetail);
        doReturn(List.of(trigger)).when(scheduler).getTriggersOfJob(jobKey);
        when(scheduler.getTriggerState(triggerKey)).thenReturn(Trigger.TriggerState.NORMAL);

        List<JobResponse> result = jobService.getAllJobs();

        assertEquals(1, result.size());
        JobResponse response = result.get(0);
        assertEquals("sampleJob", response.getJobName());
        assertEquals("DEFAULT", response.getJobGroup());
        assertEquals("NORMAL", response.getJobStatus());
        assertEquals("0 0 0 * * ?", response.getCronExpression());
    }

    @Test
    @DisplayName("查詢所有 Job - 無 Trigger 標記為 REGISTERED")
    void getAllJobs_jobWithoutTrigger_returnsRegisteredStatus() throws SchedulerException {
        JobKey jobKey = JobKey.jobKey("orphanJob", "DEFAULT");
        JobDetail jobDetail = mock(JobDetail.class);
        when(jobDetail.getDescription()).thenReturn("orphan job");

        when(scheduler.getJobKeys(any(GroupMatcher.class))).thenReturn(Set.of(jobKey));
        when(scheduler.getJobDetail(jobKey)).thenReturn(jobDetail);
        when(scheduler.getTriggersOfJob(jobKey)).thenReturn(List.of());

        List<JobResponse> result = jobService.getAllJobs();

        assertEquals(1, result.size());
        assertEquals("REGISTERED", result.get(0).getJobStatus());
    }

    @Test
    @DisplayName("查詢所有 Job 失敗 - SchedulerException 轉換為 SYSTEM_ERROR")
    void getAllJobs_schedulerException_throwsSystemError() throws SchedulerException {
        when(scheduler.getJobKeys(any(GroupMatcher.class))).thenThrow(new SchedulerException("boom"));

        BusinessException exception = assertThrows(BusinessException.class, () -> jobService.getAllJobs());

        assertEquals(ResultCode.SYSTEM_ERROR, exception.getResultCode());
    }

    @Test
    @DisplayName("暫停 Job 成功")
    void pauseJob_success_callsScheduler() throws SchedulerException {
        jobService.pauseJob("sampleJob", "DEFAULT");

        verify(scheduler, times(1)).pauseJob(JobKey.jobKey("sampleJob", "DEFAULT"));
    }

    @Test
    @DisplayName("暫停 Job 失敗 - SchedulerException 轉換為 JOB_ACTION_FAILED")
    void pauseJob_schedulerException_throwsJobActionFailed() throws SchedulerException {
        doThrow(new SchedulerException("boom")).when(scheduler).pauseJob(any(JobKey.class));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> jobService.pauseJob("sampleJob", "DEFAULT"));

        assertEquals(ResultCode.JOB_ACTION_FAILED, exception.getResultCode());
    }

    @Test
    @DisplayName("恢復 Job 成功")
    void resumeJob_success_callsScheduler() throws SchedulerException {
        jobService.resumeJob("sampleJob", "DEFAULT");

        verify(scheduler, times(1)).resumeJob(JobKey.jobKey("sampleJob", "DEFAULT"));
    }

    @Test
    @DisplayName("觸發 Job 成功")
    void triggerJob_success_callsScheduler() throws SchedulerException {
        jobService.triggerJob("sampleJob", "DEFAULT");

        verify(scheduler, times(1)).triggerJob(JobKey.jobKey("sampleJob", "DEFAULT"));
    }

    @Test
    @DisplayName("更新 Job Cron 失敗 - 找不到 Job")
    void updateJobCron_jobNotFound_throwsJobNotFound() throws SchedulerException {
        JobKey jobKey = JobKey.jobKey("missingJob", "DEFAULT");
        when(scheduler.checkExists(jobKey)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> jobService.updateJobCron("missingJob", "DEFAULT", "0 0 0 * * ?"));

        assertEquals(ResultCode.JOB_NOT_FOUND, exception.getResultCode());
    }

    @Test
    @DisplayName("更新 Job Cron - 無既有 Trigger 時新建")
    void updateJobCron_noExistingTrigger_createsNewTrigger() throws SchedulerException {
        JobKey jobKey = JobKey.jobKey("newTriggerJob", "DEFAULT");
        when(scheduler.checkExists(jobKey)).thenReturn(true);
        when(scheduler.getTriggersOfJob(jobKey)).thenReturn(List.of());

        jobService.updateJobCron("newTriggerJob", "DEFAULT", "0 0 12 * * ?");

        verify(scheduler, times(1)).scheduleJob(any(Trigger.class));
    }

    @Test
    @DisplayName("更新 Job Cron - 既有 Trigger 且 cron 不同時重新排程")
    void updateJobCron_existingTriggerDifferentCron_reschedules() throws SchedulerException {
        JobKey jobKey = JobKey.jobKey("existingJob", "DEFAULT");
        CronTrigger trigger = mock(CronTrigger.class);
        TriggerKey triggerKey = TriggerKey.triggerKey("existingJobTrigger", "DEFAULT");
        when(trigger.getKey()).thenReturn(triggerKey);
        when(trigger.getCronExpression()).thenReturn("0 0 0 * * ?");

        when(scheduler.checkExists(jobKey)).thenReturn(true);
        doReturn(List.of(trigger)).when(scheduler).getTriggersOfJob(jobKey);

        jobService.updateJobCron("existingJob", "DEFAULT", "0 0 6 * * ?");

        verify(scheduler, times(1)).rescheduleJob(eq(triggerKey), any(Trigger.class));
    }

    @Test
    @DisplayName("更新 Job Cron - 既有 Trigger 且 cron 相同時不重新排程")
    void updateJobCron_existingTriggerSameCron_noReschedule() throws SchedulerException {
        JobKey jobKey = JobKey.jobKey("sameCronJob", "DEFAULT");
        CronTrigger trigger = mock(CronTrigger.class);
        when(trigger.getCronExpression()).thenReturn("0 0 0 * * ?");

        when(scheduler.checkExists(jobKey)).thenReturn(true);
        doReturn(List.of(trigger)).when(scheduler).getTriggersOfJob(jobKey);

        jobService.updateJobCron("sameCronJob", "DEFAULT", "0 0 0 * * ?");

        verify(scheduler, never()).rescheduleJob(any(TriggerKey.class), any(Trigger.class));
    }
}
