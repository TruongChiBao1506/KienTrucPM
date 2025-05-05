package iuh.fit.se.chatbotservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String message;
    private String conversationId;
    private Boolean productSuggestion;
    private ProductSuggestion[] suggestedProducts;
    private NavigationSuggestion navigationSuggestion;
    private Boolean hasNavigationSuggestion;
}