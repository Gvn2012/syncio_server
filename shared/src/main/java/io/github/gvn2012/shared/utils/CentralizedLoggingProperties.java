package io.github.gvn2012.shared.utils;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "syncio.centralized-logging")
public class CentralizedLoggingProperties {

    private boolean enabled = true;
    private String topic = "centralized-logs";
    private String environment = System.getenv().getOrDefault("SYNCIO_ENVIRONMENT", "unknown");
    private boolean logHttpRequests = true;
    private boolean logSuccessfulHttpRequests = true;
    private List<String> excludedPaths = new ArrayList<>(List.of(
            "/actuator",
            "/actuator/**",
            "/favicon.ico"
    ));

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public boolean isLogHttpRequests() {
        return logHttpRequests;
    }

    public void setLogHttpRequests(boolean logHttpRequests) {
        this.logHttpRequests = logHttpRequests;
    }

    public boolean isLogSuccessfulHttpRequests() {
        return logSuccessfulHttpRequests;
    }

    public void setLogSuccessfulHttpRequests(boolean logSuccessfulHttpRequests) {
        this.logSuccessfulHttpRequests = logSuccessfulHttpRequests;
    }

    public List<String> getExcludedPaths() {
        return excludedPaths;
    }

    public void setExcludedPaths(List<String> excludedPaths) {
        this.excludedPaths = excludedPaths;
    }
}
