package org.rookies.zdme.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendMail(String to, String subject, String text, boolean isHtml) {
        if (mailSender == null) {
            log.warn("MailSender is not configured. Logging email content instead.");
            log.info("Sending email to: {}", to);
            log.info("Subject: {}", subject);
            log.info("Body: {}", text);
            return;
        }

        if (isHtml) {
            sendHtmlMail(to, subject, text);
        } else {
            sendTextMail(to, subject, text);
        }
    }

    private void sendTextMail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Text email sent successfully to {}", to);
        } catch (MailException e) {
            log.error("Error sending text email to {}", to, e);
        }
    }

    private void sendHtmlMail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("HTML email sent successfully to {}", to);
        } catch (MessagingException | MailException e) {
            log.error("Error sending HTML email to {}", to, e);
        }
    }
}
