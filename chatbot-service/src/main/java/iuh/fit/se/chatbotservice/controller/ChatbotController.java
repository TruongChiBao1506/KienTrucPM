package iuh.fit.se.chatbotservice.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import iuh.fit.se.chatbotservice.dto.ChatRequest;
import iuh.fit.se.chatbotservice.dto.ChatResponse;
import iuh.fit.se.chatbotservice.dto.NavigationSuggestion;
import iuh.fit.se.chatbotservice.model.Conversation;
import iuh.fit.se.chatbotservice.service.ChatbotService;
import iuh.fit.se.chatbotservice.service.NavigationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final NavigationService navigationService;

    @PostMapping("/chat")
    @RateLimiter(name = "chatEndpoint", fallbackMethod = "chatFallback")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatbotService.processChat(request);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<ChatResponse> chatFallback(ChatRequest request, Exception ex) {
        ChatResponse errorResponse = ChatResponse.builder()
                .message("We're experiencing high demand right now. Please try again in a moment.")
                .conversationId(request.getConversationId())
                .build();
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
    }

    @PostMapping("/check-navigation")
    @RateLimiter(name = "navigationEndpoint", fallbackMethod = "checkNavigationFallback")
    public ResponseEntity<NavigationSuggestion> checkNavigation(@RequestBody ChatRequest request) {
        NavigationSuggestion navigationSuggestion = navigationService.findNavigationSuggestion(request.getMessage());
        if (navigationSuggestion != null) {
            return ResponseEntity.ok(navigationSuggestion);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<NavigationSuggestion> checkNavigationFallback(ChatRequest request, Exception ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    @GetMapping("/conversations/{userId}")
    @RateLimiter(name = "conversationEndpoint", fallbackMethod = "getUserConversationsFallback")
    public ResponseEntity<List<Conversation>> getUserConversations(@PathVariable String userId) {
        List<Conversation> conversations = chatbotService.getUserConversations(userId);
        return ResponseEntity.ok(conversations);
    }

    public ResponseEntity<List<Conversation>> getUserConversationsFallback(String userId, Exception ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    @GetMapping("/conversation/{conversationId}")
    @RateLimiter(name = "conversationEndpoint", fallbackMethod = "getConversationFallback")
    public ResponseEntity<Conversation> getConversation(@PathVariable String conversationId) {
        Optional<Conversation> conversation = chatbotService.getConversation(conversationId);
        return conversation
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<Conversation> getConversationFallback(String conversationId, Exception ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

    @DeleteMapping("/conversation/{conversationId}")
    @RateLimiter(name = "conversationEndpoint", fallbackMethod = "deleteConversationFallback")
    public ResponseEntity<Void> deleteConversation(@PathVariable String conversationId) {
        chatbotService.deleteConversation(conversationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    public ResponseEntity<Void> deleteConversationFallback(String conversationId, Exception ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }
}