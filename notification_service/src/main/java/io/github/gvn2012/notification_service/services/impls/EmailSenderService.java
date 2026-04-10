package io.github.gvn2012.notification_service.services.impls;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import io.github.gvn2012.notification_service.services.interfaces.EmailBuilderInterface;
import io.github.gvn2012.notification_service.services.interfaces.EmailSenderServiceInterface;

@Service
@RequiredArgsConstructor
public class EmailSenderService implements EmailSenderServiceInterface, EmailBuilderInterface {

    private final JavaMailSender mailSender;

    @Override
    public void sendVerificationEmail(String toEmail, String verificationLink, String verificationCode) {

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setFrom("SyncIO ");
            helper.setSubject("Verify your email");

            String htmlContent = verificationCode != null && !verificationCode.isBlank()
                    ? buildOtpEmail(verificationCode)
                    : buildLinkEmail(verificationLink);

            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendOrganizationWelcomeEmail(String toEmail, String organizationName, String orgId) {

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setFrom("SyncIO");
            helper.setSubject("Welcome to " + organizationName);

            String htmlContent = buildOrganizationWelcomeEmail(organizationName, orgId);

            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public String buildLinkEmail(String verificationLink) {

        return """
                <div style="background:#f4f6f8;padding:40px 0;font-family:Arial,Helvetica,sans-serif;">

                    <div style="max-width:600px;margin:0 auto;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 12px rgba(0,0,0,0.08);">

                        <div style="background:#4CAF50;padding:25px 30px;text-align:center;">
                            <h1 style="color:#ffffff;margin:0;font-size:22px;">Syncio</h1>
                        </div>

                        <div style="padding:40px 30px;text-align:center;">

                            <h2 style="color:#333;margin-bottom:10px;">Welcome 👋</h2>

                            <p style="color:#666;font-size:15px;line-height:1.6;margin-bottom:30px;">
                                Thank you for joining <b>Syncio</b>.<br/>
                                Please verify your email address to activate your account.
                            </p>

                            <div style="margin:30px 0;">
                                <a href="%s"
                                   target="_blank"
                                   style="
                                       background:#4CAF50;
                                       color:#ffffff;
                                       padding:14px 28px;
                                       text-decoration:none;
                                       font-size:15px;
                                       border-radius:8px;
                                       display:inline-block;
                                       font-weight:bold;
                                   ">
                                    Verify Email
                                </a>
                            </div>

                            <p style="font-size:12px;color:#888;line-height:1.5;">
                                If the button doesn’t work, copy and paste this link:<br/>
                                <a href="%s" style="color:#4CAF50;word-break:break-all;">%s</a>
                            </p>

                            <div style="margin-top:25px;padding-top:15px;border-top:1px solid #eee;">
                                <p style="font-size:13px;color:#999;">
                                    ⏳ This link will expire in <b>15 minutes</b>.
                                </p>
                                <p style="font-size:12px;color:#bbb;">
                                    If you didn’t request this email, you can safely ignore it.
                                </p>
                            </div>

                        </div>

                        <div style="background:#fafafa;text-align:center;padding:15px;">
                            <p style="font-size:12px;color:#aaa;margin:0;">
                                © 2026 Syncio. All rights reserved.
                            </p>
                        </div>

                    </div>
                </div>
                """
                .formatted(verificationLink, verificationLink, verificationLink);
    }

    @Override
    public String buildOtpEmail(String verificationCode) {
        return """
                <div style="background:#f4f6f8;padding:40px 0;font-family:Arial,Helvetica,sans-serif;">

                    <div style="max-width:600px;margin:0 auto;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 12px rgba(0,0,0,0.08);">

                        <div style="background:#4CAF50;padding:25px 30px;text-align:center;">
                            <h1 style="color:#ffffff;margin:0;font-size:22px;">Syncio</h1>
                        </div>

                        <div style="padding:40px 30px;text-align:center;">
                            <h2 style="color:#333;margin-bottom:10px;">Verify your email</h2>
                            <p style="color:#666;font-size:15px;line-height:1.6;margin-bottom:30px;">
                                Enter this 6-digit code to complete your registration.
                            </p>
                            <div style="margin:30px auto;padding:16px 24px;max-width:220px;border-radius:10px;background:#f3f7f4;border:1px solid #d7e7da;font-size:32px;letter-spacing:8px;font-weight:bold;color:#1f3b26;">
                                %s
                            </div>
                            <div style="margin-top:25px;padding-top:15px;border-top:1px solid #eee;">
                                <p style="font-size:13px;color:#999;">
                                    ⏳ This code will expire in <b>10 minutes</b>.
                                </p>
                                <p style="font-size:12px;color:#bbb;">
                                    If you didn’t request this email, you can safely ignore it.
                                </p>
                            </div>
                        </div>

                        <div style="background:#fafafa;text-align:center;padding:15px;">
                            <p style="font-size:12px;color:#aaa;margin:0;">
                                © 2026 Syncio. All rights reserved.
                            </p>
                        </div>

                    </div>
                </div>
                """
                .formatted(verificationCode);
    }

    @Override
    public String buildOrganizationWelcomeEmail(String organizationName, String orgId) {
        return """
                <div style="background:#f4f6f8;padding:40px 0;font-family:Arial,Helvetica,sans-serif;">

                    <div style="max-width:600px;margin:0 auto;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 12px rgba(0,0,0,0.08);">

                        <div style="background:#4CAF50;padding:25px 30px;text-align:center;">
                            <h1 style="color:#ffffff;margin:0;font-size:22px;">Syncio</h1>
                        </div>

                        <div style="padding:40px 30px;text-align:center;">

                            <h2 style="color:#333;margin-bottom:10px;">Welcome 👋</h2>

                            <p style="color:#666;font-size:15px;line-height:1.6;margin-bottom:20px;">
                                Thank you for joining <b>Syncio</b>.<br/>
                                Your organization <b>%s</b> has been created successfully.
                            </p>

                            <div style="margin:25px 0;padding:20px;background:#f8f9fa;border-radius:10px;border:1px solid #e9ecef;">
                                <p style="margin:0 0 10px 0;font-size:14px;color:#666;font-weight:bold;text-transform:uppercase;letter-spacing:1px;">Organization ID for Login</p>
                                <div style="font-family:monospace;font-size:18px;color:#1f3b26;font-weight:bold;word-break:break-all;">
                                    %s
                                </div>
                            </div>


                            <div style="margin-top:25px;padding-top:15px;border-top:1px solid #eee;">
                                <p style="font-size:12px;color:#bbb;">
                                    If you didn’t create this organization, you can safely ignore this email.
                                </p>
                            </div>

                        </div>

                        <div style="background:#fafafa;text-align:center;padding:15px;">
                            <p style="font-size:12px;color:#aaa;margin:0;">
                                © 2026 Syncio. All rights reserved.
                            </p>
                        </div>

                    </div>
                </div>
                """
                .formatted(organizationName, orgId);
    }
}
