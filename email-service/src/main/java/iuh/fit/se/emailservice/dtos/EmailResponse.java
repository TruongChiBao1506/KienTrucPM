package iuh.fit.se.emailservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailResponse {
    private String messageId;
    private String to;
    private String subject;
    private String status;
    private String message;
    private LocalDateTime sentAt;
}
