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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final PresenceService presenceService;
    private final IMessagingService messagingService;

    /**
     * Track number of active STOMP sessions per userId.
     * Only set user offline when ALL sessions for that user have disconnected.
     */
    private final Map<String, AtomicInteger> userSessionCounts = new ConcurrentHashMap<>();

    /**
     * Delayed offline tasks — cancelled if user reconnects within the grace window.
     * This prevents flickering online/offline during STOMP auto-reconnect cycles.
     */
    private final Map<String, ScheduledFuture<?>> pendingOfflineTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "presence-offline-scheduler");
        t.setDaemon(true);
        return t;
    });

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = headerAccessor.getFirstNativeHeader("X-User-Id");
        if (userId != null) {
            ScheduledFuture<?> pendingOffline = pendingOfflineTasks.remove(userId);
            if (pendingOffline != null) {
                pendingOffline.cancel(false);
                log.info("User {} reconnected, cancelled pending offline", userId);
            }

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
                final String uid = userId;
                ScheduledFuture<?> task = scheduler.schedule(() -> {
                    pendingOfflineTasks.remove(uid);
                    AtomicInteger currentCount = userSessionCounts.get(uid);
                    if (currentCount == null || currentCount.get() <= 0) {
                        log.info("User {} confirmed offline after grace period", uid);
                        presenceService.setUserOffline(uid);
                    }
                }, 5, TimeUnit.SECONDS);
                pendingOfflineTasks.put(userId, task);
            }
        }
    }
}
