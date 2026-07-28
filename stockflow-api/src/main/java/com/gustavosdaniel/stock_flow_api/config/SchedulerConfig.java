package com.gustavosdaniel.stock_flow_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring configuration that enables scheduled task execution.
 * <p>
 * Activates Spring's {@code @Scheduled} annotation processing, allowing
 * periodic background jobs (e.g., data cleanup, report generation) to run
 * on a configurable thread pool.
 * </p>
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
}
