package iuh.fit.se.emailservice.services.impl;

import iuh.fit.se.emailservice.dtos.EmailRequest;
import iuh.fit.se.emailservice.dtos.EmailResponse;
import iuh.fit.se.emailservice.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Override
    public EmailResponse sendEmail(EmailRequest emailRequest) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailRequest.getTo());
            message.setSubject(emailRequest.getSubject());
            message.setText(emailRequest.getBody());
            
            if (emailRequest.getCc() != null && !emailRequest.getCc().isEmpty()) {
                message.setCc(emailRequest.getCc());
            }
            
            if (emailRequest.getBcc() != null && !emailRequest.getBcc().isEmpty()) {
                message.setBcc(emailRequest.getBcc());
            }
            
            javaMailSender.send(message);
            
            return EmailResponse.builder()
                    .messageId(UUID.randomUUID().toString())
                    .to(emailRequest.getTo())
                    .subject(emailRequest.getSubject())
                    .status("SUCCESS")
                    .message("Email đã được gửi thành công")
                    .sentAt(LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            return EmailResponse.builder()
                    .status("FAILED")
                    .message("Không thể gửi email: " + e.getMessage())
                    .sentAt(LocalDateTime.now())
                    .build();
        }
    }

    @Override
    public EmailResponse sendHtmlEmail(EmailRequest emailRequest) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(emailRequest.getTo());
            helper.setSubject(emailRequest.getSubject());
            helper.setText(emailRequest.getBody(), true);
            
            if (emailRequest.getCc() != null && !emailRequest.getCc().isEmpty()) {
                helper.setCc(emailRequest.getCc());
            }
            
            if (emailRequest.getBcc() != null && !emailRequest.getBcc().isEmpty()) {
                helper.setBcc(emailRequest.getBcc());
            }
            
            javaMailSender.send(message);
            
            return EmailResponse.builder()
                    .messageId(UUID.randomUUID().toString())
                    .to(emailRequest.getTo())
                    .subject(emailRequest.getSubject())
                    .status("SUCCESS")
                    .message("Email HTML đã được gửi thành công")
                    .sentAt(LocalDateTime.now())
                    .build();
                    
        } catch (MessagingException e) {
            return EmailResponse.builder()
                    .status("FAILED")
                    .message("Không thể gửi email HTML: " + e.getMessage())
                    .sentAt(LocalDateTime.now())
                    .build();
        }
    }
}
