package com.paro.newsfeed.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * This configuration class configures the schedulers for parsers in BotService.class
 */
@EnableScheduling
@Configuration
public class ParserScheduler {
}
