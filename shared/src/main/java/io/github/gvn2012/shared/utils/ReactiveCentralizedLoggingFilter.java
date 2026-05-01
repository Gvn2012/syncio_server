package io.github.gvn2012.shared.utils;

import io.github.gvn2012.shared.kafka_events.CentralizedLogEvent;
import io.micrometer.tracing.Tracer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Map;

public class ReactiveCentralizedLoggingFilter implements WebFilter {

    private final CentralizedLoggingProperties properties;
    private final CentralizedLogProducer centralizedLogProducer;
    private final Tracer tracer;

    public ReactiveCentralizedLoggingFilter(
            CentralizedLoggingProperties properties,
            CentralizedLogProducer centralizedLogProducer,
            Tracer tracer) {
        this.properties = properties;
        this.centralizedLogProducer = centralizedLogProducer;
        this.tracer = tracer;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (!properties.isEnabled()
                || !properties.isLogHttpRequests()
                || CentralizedLoggingSupport.matchesExcludedPath(path, properties)) {
            return chain.filter(exchange);
        }

        String requestId = CentralizedLoggingSupport.getOrCreateRequestId(
                exchange.getRequest().getHeaders().getFirst(CentralizedLoggingHeaders.REQUEST_ID));
        String userId = exchange.getRequest().getHeaders().getFirst(CentralizedLoggingHeaders.USER_ID);
        String sessionId = exchange.getRequest().getHeaders().getFirst(CentralizedLoggingHeaders.SESSION_ID);
        String orgId = exchange.getRequest().getHeaders().getFirst(CentralizedLoggingHeaders.ORG_ID);
        long startedAt = System.currentTimeMillis();

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(CentralizedLoggingHeaders.REQUEST_ID, requestId)
                .build();
        exchange.getResponse().getHeaders().set(CentralizedLoggingHeaders.REQUEST_ID, requestId);

        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
        return chain.filter(mutatedExchange)
                .doOnSuccess(ignored -> emitLog(mutatedExchange, requestId, userId, sessionId, orgId, startedAt, null))
                .doOnError(error -> emitLog(mutatedExchange, requestId, userId, sessionId, orgId, startedAt, error));
    }

    private void emitLog(
            ServerWebExchange exchange,
            String requestId,
            String userId,
            String sessionId,
            String orgId,
            long startedAt,
            Throwable error) {
        int statusCode = exchange.getResponse().getStatusCode() != null
                ? exchange.getResponse().getStatusCode().value()
                : 200;
        String outcome = statusCode >= 500 || error != null ? "ERROR" : "SUCCESS";
        if (!properties.isLogSuccessfulHttpRequests() && "SUCCESS".equals(outcome)) {
            return;
        }

        Map<String, Object> metadata = CentralizedLoggingSupport.newMetadata();
        metadata.put("protocol", "http");
        metadata.put("httpMethod", exchange.getRequest().getMethod() != null ? exchange.getRequest().getMethod().name() : null);
        metadata.put("path", exchange.getRequest().getURI().getPath());
        metadata.put("query", exchange.getRequest().getURI().getQuery());
        metadata.put("statusCode", statusCode);
        metadata.put("durationMs", System.currentTimeMillis() - startedAt);
        CentralizedLoggingSupport.putIfHasText(metadata, "clientIp", extractClientIp(exchange));
        CentralizedLoggingSupport.putIfHasText(metadata, "userAgent", exchange.getRequest().getHeaders().getFirst("User-Agent"));
        CentralizedLoggingSupport.putIfHasText(metadata, "referer", exchange.getRequest().getHeaders().getFirst("Referer"));
        CentralizedLoggingSupport.putIfHasText(metadata, "origin", exchange.getRequest().getHeaders().getFirst("Origin"));
        CentralizedLoggingSupport.putIfHasText(metadata, "platform", exchange.getRequest().getHeaders().getFirst(CentralizedLoggingHeaders.PLATFORM));
        CentralizedLoggingSupport.putIfHasText(metadata, "timezone", exchange.getRequest().getHeaders().getFirst(CentralizedLoggingHeaders.TIMEZONE));
        CentralizedLoggingSupport.putIfHasText(metadata, "orgId", orgId);
        CentralizedLoggingSupport.putIfHasText(metadata, "routeId", resolveRouteId(exchange));
        if (error != null) {
            metadata.put("error", error.getClass().getSimpleName());
        }

        centralizedLogProducer.logAsync(CentralizedLogEvent.builder()
                .traceId(CentralizedLoggingSupport.currentTraceId(tracer))
                .spanId(CentralizedLoggingSupport.currentSpanId(tracer))
                .requestId(requestId)
                .userId(userId)
                .sessionId(sessionId)
                .severity("ERROR".equals(outcome) ? "ERROR" : "INFO")
                .action("HTTP_REQUEST")
                .outcome(outcome)
                .message((exchange.getRequest().getMethod() != null ? exchange.getRequest().getMethod().name() : "HTTP")
                        + " " + exchange.getRequest().getURI().getPath())
                .metadata(metadata)
                .build());
    }

    private String extractClientIp(ServerWebExchange exchange) {
        String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (CentralizedLoggingSupport.hasText(xff)) {
            return xff.split(",")[0].trim();
        }
        String realIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (CentralizedLoggingSupport.hasText(realIp)) {
            return realIp;
        }
        return exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : null;
    }

    private String resolveRouteId(ServerWebExchange exchange) {
        Object route = exchange.getAttributes().values().stream()
                .filter(value -> value != null && value.getClass().getName().endsWith(".Route"))
                .findFirst()
                .orElse(null);
        if (route == null) {
            return null;
        }
        try {
            return String.valueOf(route.getClass().getMethod("getId").invoke(route));
        } catch (ReflectiveOperationException ignored) {
            return route.toString();
        }
    }
}
