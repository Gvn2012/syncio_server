package io.github.gvn2012.notification_service.services.interfaces;

public interface EmailBuilderInterface {
    String buildLinkEmail(String verificationLink);

    String buildOtpEmail(String verificationCode);

    String buildOrganizationWelcomeEmail(String organizationName, String orgId);
}
