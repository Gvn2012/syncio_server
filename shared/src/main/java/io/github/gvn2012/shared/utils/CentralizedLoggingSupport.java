package io.github.gvn2012.shared.utils;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.MDC;
import org.springframework.util.AntPathMatcher;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class CentralizedLoggingSupport {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private CentralizedLoggingSupport() {
    }

    public static String currentTraceId(Tracer tracer) {
        String traceId = MDC.get(CentralizedLoggingMdc.TRACE_ID);
        if (hasText(traceId)) {
            return traceId;
        }
        if (tracer == null) {
            return null;
        }
        Span span = tracer.currentSpan();
        return span != null ? span.context().traceId() : null;
    }

    public static String currentSpanId(Tracer tracer) {
        String spanId = MDC.get(CentralizedLoggingMdc.SPAN_ID);
        if (hasText(spanId)) {
            return spanId;
        }
        if (tracer == null) {
            return null;
        }
        Span span = tracer.currentSpan();
        return span != null ? span.context().spanId() : null;
    }

    public static String getOrCreateRequestId(String requestId) {
        return hasText(requestId) ? requestId : UUID.randomUUID().toString();
    }

    public static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public static boolean matchesExcludedPath(String path, CentralizedLoggingProperties properties) {
        if (!hasText(path)) {
            return false;
        }
        return properties.getExcludedPaths().stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    public static Map<String, Object> newMetadata() {
        return new LinkedHashMap<>();
    }

    public static void putIfHasText(Map<String, Object> metadata, String key, String value) {
        if (hasText(value)) {
            metadata.put(key, value);
        }
    }

    public static void putIfNotNull(Map<String, Object> metadata, String key, Object value) {
        if (value != null) {
            metadata.put(key, value);
        }
    }

    public static Instant now() {
        return Instant.now();
    }
}
