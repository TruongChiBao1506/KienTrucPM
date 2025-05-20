package iuh.fit.se.chatbotservice.repository;

import iuh.fit.se.chatbotservice.model.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends MongoRepository<Conversation, String> {
    List<Conversation> findByUserId(String userId);
    Optional<Conversation> findByIdAndUserId(String id, String userId);
}