package org.example.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
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
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Reset password Merkador");
        message.setText("Click on the following link to reset your password:\n" +
                "http://localhost:5173/reset-password?token=" + token);
        mailSender.send(message);
    }
}