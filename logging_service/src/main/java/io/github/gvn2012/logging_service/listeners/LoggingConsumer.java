package io.github.gvn2012.logging_service.listeners;

import io.github.gvn2012.logging_service.entities.CentralizedLog;
import io.github.gvn2012.logging_service.repositories.CentralizedLogRepository;
import io.github.gvn2012.shared.kafka_events.CentralizedLogEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoggingConsumer {

    private final CentralizedLogRepository logRepository;

    @KafkaListener(topics = "centralized-logs", groupId = "logging-service-group")
    public void consume(CentralizedLogEvent event) {
        try {
            if (event.getId() != null && logRepository.existsById(event.getId())) {
                log.debug("Skipping duplicate centralized log event id={}", event.getId());
                return;
            }

            CentralizedLog centralizedLog = CentralizedLog.builder()
                    .id(event.getId())
                    .timestamp(event.getTimestamp())
                    .severity(event.getSeverity())
                    .service(event.getService())
                    .environment(event.getEnvironment())
                    .message(event.getMessage())
                    .traceId(event.getTraceId())
                    .spanId(event.getSpanId())
                    .requestId(event.getRequestId())
                    .userId(event.getUserId())
                    .sessionId(event.getSessionId())
                    .action(event.getAction())
                    .outcome(event.getOutcome())
                    .metadata(event.getMetadata())
                    .build();

            logRepository.save(centralizedLog);
            log.debug("Saved centralized log event id={} service={} action={}",
                    centralizedLog.getId(), centralizedLog.getService(), centralizedLog.getAction());
        } catch (Exception e) {
            log.error("Failed to process centralized log event id={}", event.getId(), e);
        }
    }
}
