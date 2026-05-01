package io.github.gvn2012.logging_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LogArchiveProperties.class)
public class LoggingServiceConfig {
}
