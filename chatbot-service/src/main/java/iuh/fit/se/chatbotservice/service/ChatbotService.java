package iuh.fit.se.chatbotservice.service;

import iuh.fit.se.chatbotservice.dto.ChatRequest;
import iuh.fit.se.chatbotservice.dto.ChatResponse;
import iuh.fit.se.chatbotservice.model.Conversation;
import iuh.fit.se.chatbotservice.model.Message;
import iuh.fit.se.chatbotservice.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final ConversationRepository conversationRepository;
    private final GeminiService geminiService;

    public ChatResponse processChat(ChatRequest request) {
        String conversationId = request.getConversationId();
        String userId = request.getUserId();

        if (userId == null || userId.isBlank()) {
            userId = "anonymous-" + UUID.randomUUID().toString();
        }

        // Lấy hoặc tạo cuộc hội thoại
        Conversation conversation;

        if (conversationId != null && !conversationId.isBlank()) {
            // Tìm cuộc hội thoại đã tồn tại
            Optional<Conversation> existingConversation = conversationRepository.findById(conversationId);

            if (existingConversation.isPresent()) {
                conversation = existingConversation.get();
            } else {
                // Tạo cuộc hội thoại mới nếu không tìm thấy
                conversation = createNewConversation(userId, request.getLanguage());
            }
        } else {
            // Tạo cuộc hội thoại mới
            conversation = createNewConversation(userId, request.getLanguage());
        }

        // Thêm tin nhắn từ người dùng vào cuộc hội thoại
        Message userMessage = Message.builder()
                .role("user")
                .content(request.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        conversation.getMessages().add(userMessage);

        // Cập nhật thời gian cuộc hội thoại
        conversation.setUpdatedAt(LocalDateTime.now());

        // Lưu cuộc hội thoại trước khi gọi AI để đảm bảo tin nhắn của người dùng được lưu
        conversation = conversationRepository.save(conversation);

        // Gọi AI để xử lý tin nhắn
        ChatResponse response = geminiService.generateChatResponse(request, conversation);

        // Thêm tin nhắn từ AI vào cuộc hội thoại
        Message assistantMessage = Message.builder()
                .role("assistant")
                .content(response.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        conversation.getMessages().add(assistantMessage);

        // Lưu cuộc hội thoại sau khi đã thêm phản hồi của AI
        conversationRepository.save(conversation);

        return response;
    }

    public List<Conversation> getUserConversations(String userId) {
        return conversationRepository.findByUserId(userId);
    }

    public Optional<Conversation> getConversation(String conversationId) {
        return conversationRepository.findById(conversationId);
    }

    public void deleteConversation(String conversationId) {
        conversationRepository.deleteById(conversationId);
    }

    private Conversation createNewConversation(String userId, String language) {
        LocalDateTime now = LocalDateTime.now();
        return Conversation.builder()
                .userId(userId)
                .messages(new ArrayList<>())
                .createdAt(now)
                .updatedAt(now)
                .language(language != null && !language.isBlank() ? language : "vi")
                .build();
    }
}