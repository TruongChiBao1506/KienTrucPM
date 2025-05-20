package iuh.fit.se.emailservice.events.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.se.emailservice.dtos.OtpEmailEvent;
import iuh.fit.se.emailservice.entities.Order;
import iuh.fit.se.emailservice.entities.User;
import iuh.fit.se.emailservice.feign.UserServiceClient;
import iuh.fit.se.emailservice.utils.EmailTemplateUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EmailEventListener {
    @Autowired
    private UserServiceClient userServiceClient;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JavaMailSender javaMailSender;

    @KafkaListener(topics = "email.topic", groupId = "email-service-group")
    public void handleEmailEvents(String message){
        System.out.println(message);
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            String eventType = jsonNode.get("eventType").asText();
            switch (eventType) {
                case "sendEmail":
                    Order order = objectMapper.treeToValue(jsonNode.get("order"), Order.class);
                    String email = jsonNode.get("email").asText();
                    String productTable = objectMapper.treeToValue(jsonNode.get("productTable"), String.class);
                    System.out.println("Received order: " + order);
                    StringBuilder productTableConvert = new StringBuilder(productTable);
                    handleEmailCreated(order, email, productTableConvert);
                    System.out.println("created email successfully");
                    break;
                case "SendOtpEmail":
                    // Handle SendOtpEmail event
                    OtpEmailEvent otpEmailEvent = objectMapper.treeToValue(jsonNode.get("data"), OtpEmailEvent.class);
                    String otpEmail = otpEmailEvent.getEmail();
                    String otp = otpEmailEvent.getOtp();
                    handleSendOTPEmail(otpEmail, otp);
                    System.out.println("Sent OTP email to: " + otpEmail);
                    break;
                case "SendResetPasswordEmail":
                    // Handle SendResetPasswordEmail event
                    String emailReset = jsonNode.get("email").asText();
                    String token = jsonNode.get("token").asText();
                    handleSendResetPasswordEmail(emailReset, token);
                    System.out.println("Sent reset password email to: " + emailReset);
                    break;
                default:
                    System.out.println("Unknown event type: " + eventType);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleSendOTPEmail(String email, String otp){
        String subject = "OTP Email";
        String htmlContent = EmailTemplateUtil.buildOtpEmailContent(otp);
        MimeMessage messageMail = javaMailSender.createMimeMessage();
        try{
            MimeMessageHelper helper = new MimeMessageHelper(messageMail, true);
            helper.setFrom("sendingemaileventhub@gmail.com");
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            javaMailSender.send(messageMail);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void handleEmailCreated(Order order, String email, StringBuilder productTable) {
        ResponseEntity<Map<String, Object>> response = userServiceClient.getUserById(order.getUserId());
        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> userData = response.getBody();
            if (userData != null) {
                Object userObj = response.getBody().get("data");
                User user = objectMapper.convertValue(userObj, User.class);
                String subject = "Xác nhận đơn hàng #" + order.getOrderNumber();
                String htmlContent = EmailTemplateUtil.buildEmailContent(order, user, productTable);
                MimeMessage messageMail = javaMailSender.createMimeMessage();
                try{
                    MimeMessageHelper helper = new MimeMessageHelper(messageMail, true);
                    helper.setFrom("sendingemaileventhub@gmail.com");
                    helper.setTo(email);
                    helper.setSubject(subject);
                    helper.setText(htmlContent, true);
                    javaMailSender.send(messageMail);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("Failed to fetch user data");

        }
    }
    public void handleSendResetPasswordEmail(String email, String token){
        String resetUrl = "http://localhost:8889/reset-password?token=" + token;
        String subject = "Reset Your Password";
//        String message = "Click the link below to reset your password:\n" + resetUrl;
        // HTML Template for Email
        String htmlContent = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px;\">"
                + "<h2 style=\"color: #333;\">Password Reset Request</h2>"
                + "<p>We received a request to reset your password. Click the link below to reset your password:</p>"
                + "<a href=\"" + resetUrl
                + "\" style=\"display: inline-block; padding: 10px 20px; margin: 20px 0; font-size: 16px; color: #fff; background-color: #007bff; text-decoration: none; border-radius: 5px;\">Reset Password</a>"
                + "<p>If you did not request a password reset, please ignore this email or contact support if you have questions.</p>"
                + "<p>Thank you,<br>Your Company Team</p>" + "</div>";

        MimeMessage messageMail = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(messageMail, true);
            helper.setFrom("sendingemaileventhub@gmail.com");
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            javaMailSender.send(messageMail);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
