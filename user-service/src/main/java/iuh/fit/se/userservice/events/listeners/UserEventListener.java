package iuh.fit.se.userservice.events.listeners;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.se.userservice.events.dtos.UserEmailUpdateEvent;
import iuh.fit.se.userservice.repositories.UserRepository;
import iuh.fit.se.userservice.events.dtos.UserProfileCreatedEvent;
import iuh.fit.se.userservice.entities.User;
import iuh.fit.se.userservice.services.UserIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class UserEventListener {
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentMap<Long, CompletableFuture<Boolean>> emailUpdateFutures=new ConcurrentHashMap<>();;

    @Autowired
    private UserIndexService userIndexService;

    public UserEventListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @KafkaListener(topics = "user.topic", groupId = "user-service-group")
    public void handleUserEvents(String message){
        System.out.println(message);
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            String eventType = jsonNode.get("eventType").asText();
            switch (eventType){
                case "UserProfileCreated":
                    UserProfileCreatedEvent profileEvent = objectMapper.treeToValue(jsonNode.get("data"), UserProfileCreatedEvent.class);
                    handleUserProfileCreated(profileEvent);
                    userIndexService.addUserToElasticsearch(profileEvent.getUserId());
                    break;
                case "UserEmailResponse":
                    UserEmailUpdateEvent event = objectMapper.treeToValue(jsonNode.get("data"), UserEmailUpdateEvent.class);
                    CompletableFuture<Boolean> future = emailUpdateFutures.remove(event.getUserId());
                    if(future != null){
                        future.complete(true);
                    }
                    break;
                case "AsyncElasticsearch":
                    Long userId = objectMapper.treeToValue(jsonNode.get("userId"), Long.class);
                    System.out.println("AsyncElasticsearch: " + userId);
                    userIndexService.addUserToElasticsearch(userId);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void handleUserProfileCreated(UserProfileCreatedEvent event) {
        // Tạo user mới và gửi yêu cầu cập nhật email đến Auth-Service
        User user = new User();
        user.setUserId(event.getUserId());
        user.setUsername(event.getUsername());
        user.setFullname(event.getFullname());
        user.setDob(event.getDob());
        user.setPhone(event.getPhone());
        user.setAddress(event.getAddress());
        user.setGender(event.isGender());

        userRepository.save(user);
        userIndexService.addUserToElasticsearch(event.getUserId());

    }
    public CompletableFuture<Boolean> waitForEmailUpdate(Long userId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        emailUpdateFutures.put(userId, future);
        return future;
    }
}
