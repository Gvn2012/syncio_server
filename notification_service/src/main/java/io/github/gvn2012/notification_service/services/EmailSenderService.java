package io.github.gvn2012.notification_service.services;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String verificationLink) {

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setFrom("SyncIO ");
            helper.setSubject("Verify your email");

            String htmlContent = buildHtmlEmail(verificationLink);

            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildHtmlEmail(String verificationLink) {

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
                """.formatted(verificationLink, verificationLink, verificationLink);
    }
}