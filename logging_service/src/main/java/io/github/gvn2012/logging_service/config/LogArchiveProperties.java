package io.github.gvn2012.logging_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.ZoneOffset;

@ConfigurationProperties(prefix = "syncio.logging.archive")
public class LogArchiveProperties {

    private boolean enabled = true;
    private int retentionDays = 30;
    private int batchSize = 500;
    private String directory = "./data/log-archive";
    private String cron = "0 15 2 * * *";
    private ZoneOffset zoneOffset = ZoneOffset.UTC;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getRetentionDays() {
        return retentionDays;
    }

    public void setRetentionDays(int retentionDays) {
        this.retentionDays = retentionDays;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public ZoneOffset getZoneOffset() {
        return zoneOffset;
    }

    public void setZoneOffset(ZoneOffset zoneOffset) {
        this.zoneOffset = zoneOffset;
    }
}
