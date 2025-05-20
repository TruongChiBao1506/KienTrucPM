package iuh.fit.se.chatbotservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    @NotBlank
    private String message;
    private String userId;
    private String conversationId;
    private String language;
}