package iuh.fit.se.authservice.events.listeners;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.se.authservice.entities.User;
import iuh.fit.se.authservice.events.dtos.UserEmailUpdateEvent;
import iuh.fit.se.authservice.events.publishers.UserEventPublisher;
import iuh.fit.se.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class UserEventListener {
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserEventPublisher userEventPublisher;

    public UserEventListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @KafkaListener(topics = "auth.topic", groupId = "auth-service-group")
    public void handleUserEvents(String message) {
        System.out.println(message);
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            System.out.println(jsonNode.toString());
            String eventType = jsonNode.get("eventType").asText();
            switch (eventType) {
                case "UserEmailUpdate":
                    UserEmailUpdateEvent profileEvent = objectMapper.treeToValue(jsonNode.get("data"), UserEmailUpdateEvent.class);
                    handleUserEmailUpdate(profileEvent);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleUserEmailUpdate(UserEmailUpdateEvent event) {
        // Cập nhật email cho user
        User user = userRepository.findById(event.getUserId()).get();
        user.setEmail(event.getEmail());
        userRepository.save(user);

        userEventPublisher.publishUserEmailResponse(event);
    }

}
