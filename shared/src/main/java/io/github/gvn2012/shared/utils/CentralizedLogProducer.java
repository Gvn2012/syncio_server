package io.github.gvn2012.shared.utils;

import io.github.gvn2012.shared.kafka_events.CentralizedLogEvent;
import io.micrometer.tracing.Tracer;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.kafka.core.KafkaTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class CentralizedLogProducer {

    private static final Logger log = LoggerFactory.getLogger(CentralizedLogProducer.class);

    private final KafkaTemplate<String, CentralizedLogEvent> kafkaTemplate;
    private final TaskExecutor taskExecutor;
    private final CentralizedLoggingProperties properties;
    private final Tracer tracer;
    private final String serviceName;

    public CentralizedLogProducer(
            @Qualifier("centralizedLogKafkaTemplate") KafkaTemplate<String, CentralizedLogEvent> kafkaTemplate,
            @Qualifier("centralizedLoggingTaskExecutor") TaskExecutor taskExecutor,
            CentralizedLoggingProperties properties,
            Tracer tracer,
            @Value("${spring.application.name:unknown-service}") String serviceName) {
        this.kafkaTemplate = kafkaTemplate;
        this.taskExecutor = taskExecutor;
        this.properties = properties;
        this.tracer = tracer;
        this.serviceName = serviceName;
    }

    public void logAsync(String severity, String message, String action, String outcome, Map<String, Object> metadata) {
        logAsync(CentralizedLogEvent.builder()
                .severity(severity)
                .message(message)
                .action(action)
                .outcome(outcome)
                .metadata(metadata)
                .build());
    }

    public void logAsync(CentralizedLogEvent event) {
        if (!properties.isEnabled()) {
            return;
        }

        CentralizedLogEvent payload = enrich(event);
        taskExecutor.execute(() -> kafkaTemplate.send(properties.getTopic(), payload.getId(), payload)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.warn("Failed to publish centralized log event action={} service={}",
                                payload.getAction(), payload.getService(), error);
                    }
                }));
    }

    private CentralizedLogEvent enrich(CentralizedLogEvent event) {
        String requestId = firstNonBlank(event.getRequestId(), MDC.get(CentralizedLoggingMdc.REQUEST_ID));
        String userId = firstNonBlank(event.getUserId(), MDC.get(CentralizedLoggingMdc.USER_ID));
        String sessionId = firstNonBlank(event.getSessionId(), MDC.get(CentralizedLoggingMdc.SESSION_ID));
        return CentralizedLogEvent.builder()
                .id(firstNonBlank(event.getId(), UUID.randomUUID().toString()))
                .timestamp(event.getTimestamp() != null ? event.getTimestamp() : Instant.now())
                .severity(firstNonBlank(event.getSeverity(), "INFO"))
                .service(firstNonBlank(event.getService(), serviceName))
                .environment(firstNonBlank(event.getEnvironment(), properties.getEnvironment()))
                .message(event.getMessage())
                .traceId(firstNonBlank(event.getTraceId(), CentralizedLoggingSupport.currentTraceId(tracer)))
                .spanId(firstNonBlank(event.getSpanId(), CentralizedLoggingSupport.currentSpanId(tracer)))
                .requestId(requestId)
                .userId(userId)
                .sessionId(sessionId)
                .action(event.getAction())
                .outcome(event.getOutcome())
                .metadata(event.getMetadata())
                .build();
    }

    private String firstNonBlank(String first, String second) {
        return CentralizedLoggingSupport.hasText(first) ? first : second;
    }
}
