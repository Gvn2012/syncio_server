package io.github.gvn2012.notification_service.services.interfaces;

public interface EmailSenderServiceInterface {
    void sendVerificationEmail(String to, String verificationLink, String verificationCode);

    void sendOrganizationWelcomeEmail(String to, String organizationName, String orgId);
}
