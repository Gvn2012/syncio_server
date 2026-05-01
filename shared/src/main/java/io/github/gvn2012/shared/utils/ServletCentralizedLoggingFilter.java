package io.github.gvn2012.shared.utils;

import io.github.gvn2012.shared.kafka_events.CentralizedLogEvent;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

public class ServletCentralizedLoggingFilter extends OncePerRequestFilter {

    private final CentralizedLoggingProperties properties;
    private final CentralizedLogProducer centralizedLogProducer;
    private final Tracer tracer;
    private final String serviceName;

    public ServletCentralizedLoggingFilter(
            CentralizedLoggingProperties properties,
            CentralizedLogProducer centralizedLogProducer,
            Tracer tracer,
            String serviceName) {
        this.properties = properties;
        this.centralizedLogProducer = centralizedLogProducer;
        this.tracer = tracer;
        this.serviceName = serviceName;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !properties.isEnabled()
                || !properties.isLogHttpRequests()
                || CentralizedLoggingSupport.matchesExcludedPath(request.getRequestURI(), properties);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = CentralizedLoggingSupport.getOrCreateRequestId(request.getHeader(CentralizedLoggingHeaders.REQUEST_ID));
        String userId = request.getHeader(CentralizedLoggingHeaders.USER_ID);
        String sessionId = request.getHeader(CentralizedLoggingHeaders.SESSION_ID);
        String orgId = request.getHeader(CentralizedLoggingHeaders.ORG_ID);
        long startedAt = System.currentTimeMillis();

        response.setHeader(CentralizedLoggingHeaders.REQUEST_ID, requestId);
        request.setAttribute(CentralizedLoggingHeaders.REQUEST_ID, requestId);

        MDC.put(CentralizedLoggingMdc.SERVICE, serviceName);
        MDC.put(CentralizedLoggingMdc.REQUEST_ID, requestId);
        putMdc(CentralizedLoggingMdc.USER_ID, userId);
        putMdc(CentralizedLoggingMdc.SESSION_ID, sessionId);
        putMdc(CentralizedLoggingMdc.ORG_ID, orgId);
        putMdc(CentralizedLoggingMdc.TRACE_ID, CentralizedLoggingSupport.currentTraceId(tracer));
        putMdc(CentralizedLoggingMdc.SPAN_ID, CentralizedLoggingSupport.currentSpanId(tracer));

        Throwable failure = null;
        try {
            filterChain.doFilter(request, response);
        } catch (Throwable throwable) {
            failure = throwable;
            throw throwable;
        } finally {
            emitLog(request, response, requestId, userId, sessionId, orgId, startedAt, failure);
            MDC.remove(CentralizedLoggingMdc.REQUEST_ID);
            MDC.remove(CentralizedLoggingMdc.USER_ID);
            MDC.remove(CentralizedLoggingMdc.SESSION_ID);
            MDC.remove(CentralizedLoggingMdc.ORG_ID);
        }
    }

    private void emitLog(
            HttpServletRequest request,
            HttpServletResponse response,
            String requestId,
            String userId,
            String sessionId,
            String orgId,
            long startedAt,
            Throwable failure) {
        String outcome = response.getStatus() >= 500 || failure != null ? "ERROR" : "SUCCESS";
        if (!properties.isLogSuccessfulHttpRequests() && "SUCCESS".equals(outcome)) {
            return;
        }

        Map<String, Object> metadata = CentralizedLoggingSupport.newMetadata();
        metadata.put("protocol", "http");
        metadata.put("httpMethod", request.getMethod());
        metadata.put("path", request.getRequestURI());
        metadata.put("query", request.getQueryString());
        metadata.put("statusCode", response.getStatus());
        metadata.put("durationMs", System.currentTimeMillis() - startedAt);
        CentralizedLoggingSupport.putIfHasText(metadata, "clientIp", extractClientIp(request));
        CentralizedLoggingSupport.putIfHasText(metadata, "userAgent", request.getHeader("User-Agent"));
        CentralizedLoggingSupport.putIfHasText(metadata, "referer", request.getHeader("Referer"));
        CentralizedLoggingSupport.putIfHasText(metadata, "origin", request.getHeader("Origin"));
        CentralizedLoggingSupport.putIfHasText(metadata, "platform", request.getHeader(CentralizedLoggingHeaders.PLATFORM));
        CentralizedLoggingSupport.putIfHasText(metadata, "timezone", request.getHeader(CentralizedLoggingHeaders.TIMEZONE));
        CentralizedLoggingSupport.putIfHasText(metadata, "locale", request.getLocale() != null ? request.getLocale().toLanguageTag() : null);
        CentralizedLoggingSupport.putIfHasText(metadata, "orgId", orgId);
        if (failure != null) {
            metadata.put("error", failure.getClass().getSimpleName());
        }

        centralizedLogProducer.logAsync(CentralizedLogEvent.builder()
                .requestId(requestId)
                .userId(userId)
                .sessionId(sessionId)
                .severity("ERROR".equals(outcome) ? "ERROR" : "INFO")
                .action("HTTP_REQUEST")
                .outcome(outcome)
                .message(request.getMethod() + " " + request.getRequestURI())
                .metadata(metadata)
                .build());
    }

    private void putMdc(String key, String value) {
        if (CentralizedLoggingSupport.hasText(value)) {
            MDC.put(key, value);
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (CentralizedLoggingSupport.hasText(xff)) {
            return xff.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        return CentralizedLoggingSupport.hasText(realIp) ? realIp : request.getRemoteAddr();
    }
}
