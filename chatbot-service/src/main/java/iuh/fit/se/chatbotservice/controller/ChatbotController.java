package iuh.fit.se.chatbotservice.controller;

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
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatbotService.processChat(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/check-navigation")
    public ResponseEntity<NavigationSuggestion> checkNavigation(@RequestBody ChatRequest request) {
        NavigationSuggestion navigationSuggestion = navigationService.findNavigationSuggestion(request.getMessage());
        if (navigationSuggestion != null) {
            return ResponseEntity.ok(navigationSuggestion);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/conversations/{userId}")
    public ResponseEntity<List<Conversation>> getUserConversations(@PathVariable String userId) {
        List<Conversation> conversations = chatbotService.getUserConversations(userId);
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<Conversation> getConversation(@PathVariable String conversationId) {
        Optional<Conversation> conversation = chatbotService.getConversation(conversationId);
        return conversation
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/conversation/{conversationId}")
    public ResponseEntity<Void> deleteConversation(@PathVariable String conversationId) {
        chatbotService.deleteConversation(conversationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}