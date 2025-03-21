package iuh.fit.se.emailservice.services;

import iuh.fit.se.emailservice.dtos.EmailRequest;
import iuh.fit.se.emailservice.dtos.EmailResponse;

public interface EmailService {
    EmailResponse sendEmail(EmailRequest emailRequest);
    EmailResponse sendHtmlEmail(EmailRequest emailRequest);
}
