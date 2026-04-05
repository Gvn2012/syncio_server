package io.github.gvn2012.shared.kafka_events;

import java.util.UUID;

public class EmailVerificationEvent {
    private UUID eventId = UUID.randomUUID();
    private UUID userId;
    private UUID emailId;

    private String email;
    private String verificationLink;
    private String verificationCode;

    public UUID getEventId() {
        return eventId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getEmailId() {
        return emailId;
    }

    public String getEmail() {
        return email;
    }

    public String getVerificationLink() {
        return verificationLink;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setEmailId(UUID emailId) {
        this.emailId = emailId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setVerificationLink(String verificationLink) {
        this.verificationLink = verificationLink;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }
}
