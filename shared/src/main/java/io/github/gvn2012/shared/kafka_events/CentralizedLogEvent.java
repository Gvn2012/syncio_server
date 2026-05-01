package io.github.gvn2012.shared.kafka_events;

import java.time.Instant;
import java.util.Map;

public class CentralizedLogEvent {

    private String id;
    private Instant timestamp;
    private String severity;
    private String service;
    private String environment;
    private String message;
    private String traceId;
    private String spanId;
    private String requestId;
    private String userId;
    private String sessionId;
    private String action;
    private String outcome;
    private Map<String, Object> metadata;

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public static final class Builder {
        private final CentralizedLogEvent target = new CentralizedLogEvent();

        public Builder id(String id) {
            target.setId(id);
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            target.setTimestamp(timestamp);
            return this;
        }

        public Builder severity(String severity) {
            target.setSeverity(severity);
            return this;
        }

        public Builder service(String service) {
            target.setService(service);
            return this;
        }

        public Builder environment(String environment) {
            target.setEnvironment(environment);
            return this;
        }

        public Builder message(String message) {
            target.setMessage(message);
            return this;
        }

        public Builder traceId(String traceId) {
            target.setTraceId(traceId);
            return this;
        }

        public Builder spanId(String spanId) {
            target.setSpanId(spanId);
            return this;
        }

        public Builder requestId(String requestId) {
            target.setRequestId(requestId);
            return this;
        }

        public Builder userId(String userId) {
            target.setUserId(userId);
            return this;
        }

        public Builder sessionId(String sessionId) {
            target.setSessionId(sessionId);
            return this;
        }

        public Builder action(String action) {
            target.setAction(action);
            return this;
        }

        public Builder outcome(String outcome) {
            target.setOutcome(outcome);
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            target.setMetadata(metadata);
            return this;
        }

        public CentralizedLogEvent build() {
            return target;
        }
    }
}
