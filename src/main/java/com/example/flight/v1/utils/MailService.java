package com.example.flight.v1.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

  private final JavaMailSender mailSender;

  public void sendMail(String to, String subject, String body) {
    try {
      to = to.trim();
      if (!to.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
        throw new IllegalArgumentException("Invalid email address: " + to);
      }

      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(body, true);
      mailSender.send(message);
    } catch (MessagingException e) {
      throw new RuntimeException("Failed to send mail", e);
    }
  }
}
