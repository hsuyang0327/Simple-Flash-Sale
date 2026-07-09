package com.flashsale.backend.job;

import com.flashsale.backend.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

/**
 * @description Quartz job to preload active flash-sale events into Redis before sale starts
 * @author Yang-Hsu
 * @date 2026/7/9
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PreloadEventsJob implements Job {

    private final EventService eventService;

    @Override
    /**
     * @description Execute event preloading into Redis for today and tomorrow
     * @author Yang-Hsu
     * @date 2026/7/9
     */
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
