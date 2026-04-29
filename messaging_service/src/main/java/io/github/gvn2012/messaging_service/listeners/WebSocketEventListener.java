package io.github.gvn2012.messaging_service.listeners;

import io.github.gvn2012.messaging_service.services.impls.PresenceService;
import io.github.gvn2012.messaging_service.services.interfaces.IMessagingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final PresenceService presenceService;
    private final IMessagingService messagingService;

    private final Map<String, AtomicInteger> userSessionCounts = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = headerAccessor.getFirstNativeHeader("X-User-Id");
        if (userId != null) {
            int count = userSessionCounts
                    .computeIfAbsent(userId, k -> new AtomicInteger(0))
                    .incrementAndGet();
            log.info("User connected: {} (active sessions: {})", userId, count);
            presenceService.setUserOnline(userId);

            messagingService.markAllAsDelivered(userId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = null;
        if (headerAccessor.getSessionAttributes() != null) {
            userId = (String) headerAccessor.getSessionAttributes().get("userId");
        }

        if (userId != null) {
            AtomicInteger sessionCount = userSessionCounts.get(userId);
            int remaining = sessionCount != null ? sessionCount.decrementAndGet() : 0;
            log.info("User disconnected: {} (remaining sessions: {})", userId, remaining);

            if (remaining <= 0) {
                userSessionCounts.remove(userId);
                presenceService.setUserOffline(userId);
            }
        }
    }
}
