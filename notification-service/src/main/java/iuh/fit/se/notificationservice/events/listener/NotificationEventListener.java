package iuh.fit.se.notificationservice.events.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.se.notificationservice.entities.Notification;
import iuh.fit.se.notificationservice.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class NotificationEventListener {
    private final NotificationRepository notificationRepository;
    @Autowired
    private ObjectMapper objectMapper;
    private final ConcurrentMap<Long, CompletableFuture<Boolean>> emailUpdateFutures=new ConcurrentHashMap<>();
    public NotificationEventListener(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @KafkaListener(topics = "notification.topic", groupId = "notification-service-group")
    public void handleNotificationEvents(String message) {
        System.out.println(message);
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            String eventType = jsonNode.get("eventType").asText();
            switch (eventType) {
                case "sendNotification":
                    Notification notification = objectMapper.treeToValue(jsonNode.get("data"), Notification.class);
                    System.out.println("Received notification: " + notification);
                    handleNotificationCreated(notification);
                    System.out.println("created notification successfully");
                    break;
                default:
                    System.out.println("Unknown event type: " + eventType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void handleNotificationCreated(Notification notification) {
        // Xử lý sự kiện NotificationCreated
        System.out.println("Notification created: " + notification);
        try {
            notificationRepository.save(notification);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}
