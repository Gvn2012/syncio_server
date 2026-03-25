package io.github.gvn2012.shared.kafka_events;

import java.util.UUID;

public class EmailVerificationEvent {
    private UUID userId;
    private UUID emailId;

    private String email;
    private String verificationLink;

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
}