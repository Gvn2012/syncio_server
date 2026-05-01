package io.github.gvn2012.messaging_service.controllers;

import io.github.gvn2012.messaging_service.dtos.APIResource;
import io.github.gvn2012.messaging_service.models.CallSession;
import io.github.gvn2012.messaging_service.services.impls.CallSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/call")
@RequiredArgsConstructor
public class CallRestController {

    private final CallSessionService callSessionService;

    @GetMapping("/session/{callId}")
    public ResponseEntity<APIResource<CallSession>> getCallSession(@PathVariable String callId) {
        Optional<CallSession> session = callSessionService.getActiveSession(callId);
        if (session.isPresent()) {
            return ResponseEntity.ok(APIResource.success(session.get()));
        } else {
            return ResponseEntity.status(404)
                    .body(APIResource.error("NOT_FOUND", "Call session has ended or does not exist", org.springframework.http.HttpStatus.NOT_FOUND, ""));
        }
    }

    @GetMapping("/active/{conversationId}")
    public ResponseEntity<APIResource<CallSession>> getActiveCallForConversation(@PathVariable String conversationId) {
        Optional<CallSession> session = callSessionService.getActiveSessionByConversation(conversationId);
        if (session.isPresent()) {
            return ResponseEntity.ok(APIResource.success(session.get()));
        } else {
            return ResponseEntity.status(404)
                    .body(APIResource.error("NOT_FOUND", "No active call for this conversation", org.springframework.http.HttpStatus.NOT_FOUND, ""));
        }
    }
}
