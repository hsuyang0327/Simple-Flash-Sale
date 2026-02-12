package com.flashsale.backend.job;

import com.flashsale.backend.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PreloadEventsJob implements Job {

    private final EventService eventService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Starting PreloadEventsJob...");
        try {
            eventService.preloadEventsForTomorrow();
            log.info("PreloadEventsJob completed successfully.");
        } catch (Exception e) {
            log.error("PreloadEventsJob failed", e);
            throw new JobExecutionException(e);
        }
    }
}
