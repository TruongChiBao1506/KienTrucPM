package iuh.fit.se.chatbotservice.service;

import iuh.fit.se.chatbotservice.dto.ChatRequest;
import iuh.fit.se.chatbotservice.dto.ChatResponse;
import iuh.fit.se.chatbotservice.model.Conversation;
import iuh.fit.se.chatbotservice.model.Message;
import iuh.fit.se.chatbotservice.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final GeminiService geminiService;

    public ChatResponse processMessage(ChatRequest request) {
        Conversation conversation;

        if (request.getConversationId() != null) {
            conversation = conversationRepository.findById(request.getConversationId())
                    .orElseGet(() -> createNewConversation(request));
        } else {
            conversation = createNewConversation(request);
        }

        // Cập nhật ngôn ngữ nếu được chỉ định
        if (request.getLanguage() != null && !request.getLanguage().isEmpty()) {
            conversation.setLanguage(request.getLanguage());
        }

        // Thêm tin nhắn của người dùng
        Message userMessage = Message.builder()
                .role("user")
                .content(request.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        conversation.getMessages().add(userMessage);

        // Lưu conversation để có ID cho việc gọi Gemini API
        conversation = conversationRepository.save(conversation);

        // Lấy phản hồi từ Gemini API
        ChatResponse response = geminiService.generateChatResponse(request, conversation);

        // Thêm phản hồi của AI vào hội thoại
        Message assistantMessage = Message.builder()
                .role("assistant")
                .content(response.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        conversation.getMessages().add(assistantMessage);

        // Cập nhật thời gian
        conversation.setUpdatedAt(LocalDateTime.now());

        // Lưu hội thoại
        conversationRepository.save(conversation);

        return response;
    }

    private Conversation createNewConversation(ChatRequest request) {
        Conversation conversation = Conversation.builder()
                .userId(request.getUserId())
                .messages(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .language(request.getLanguage() != null ? request.getLanguage() : "vi")
                .build();

        return conversation;
    }

    public List<Conversation> getUserConversations(String userId) {
        return conversationRepository.findByUserId(userId);
    }

    public Optional<Conversation> getConversation(String id) {
        return conversationRepository.findById(id);
    }

    public void deleteConversation(String id) {
        conversationRepository.deleteById(id);
    }
}