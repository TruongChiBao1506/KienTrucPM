package iuh.fit.se.chatbotservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private String role; // "user" or "assistant"
    private String content;
    private LocalDateTime timestamp;
}