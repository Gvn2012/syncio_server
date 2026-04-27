package io.github.gvn2012.messaging_service.configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
public class AuthChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String userId = accessor.getFirstNativeHeader("X-User-Id");
            if (userId != null) {
                log.info("WebSocket connection authenticated for user: {}", userId);
                Objects.requireNonNull(accessor.getSessionAttributes()).put("userId", userId);
                accessor.setUser(() -> userId);
            } else {
                log.warn("WebSocket connection attempt without X-User-Id header");
            }
        }

        return message;
    }
}
