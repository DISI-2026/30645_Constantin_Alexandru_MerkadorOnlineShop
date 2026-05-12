package org.example.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    // Will pull the URL from .env, but defaults to localhost:5173 for local dev
    private final String domainUrl;

    public EmailService(JavaMailSender mailSender, @Value("${app.domain.url:http://localhost:5173}") String domainUrl) {
        this.mailSender = mailSender;
        this.domainUrl = domainUrl;
    }

    public void sendVerificationEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your Merkador verification code");
        message.setText("Your verification code: " + code +
                "\nThis code will expire in 15 minutes. Please do not share it with anyone.");
        mailSender.send(message);
    }

    public void sendResetPasswordEmail(String to, String token) {
        try {
            // Create a MimeMessage to send HTML content
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Reset password Merkador");

            // Compose the dynamic link
            String resetLink = domainUrl + "/reset-password?token=" + token;

            // Build the HTML email body
            String htmlContent = "<h3>Password Reset Request</h3>" +
                    "<p>You requested to reset your password. Click the link below to proceed:</p>" +
                    "<p><a href=\"" + resetLink + "\" style=\"color: #3498db; font-weight: bold;\">Click here to reset your password</a></p>" +
                    "<p>If the link above does not work, copy and paste this URL into your browser:</p>" +
                    "<p>" + resetLink + "</p>";

            // The 'true' flag indicates this is an HTML email
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send reset password email", e);
        }
    }
}