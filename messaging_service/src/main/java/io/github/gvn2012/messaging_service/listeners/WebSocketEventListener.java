package io.github.gvn2012.messaging_service.listeners;

import io.github.gvn2012.messaging_service.services.impls.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final PresenceService presenceService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = headerAccessor.getFirstNativeHeader("X-User-Id");
        if (userId != null) {
            log.info("User connected: {}", userId);
            presenceService.setUserOnline(userId);
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
            log.info("User disconnected: {}", userId);
            presenceService.setUserOffline(userId);
        }
    }
}
