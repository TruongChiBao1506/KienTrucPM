package iuh.fit.se.userservice.events.publishers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import iuh.fit.se.userservice.events.dtos.UserEmailUpdateEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserEventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;  // Gửi dạng String
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    public void publishUserEmailUpdate(UserEmailUpdateEvent event) {
        try {
            // Tạo JSON object và thêm eventType
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.put("eventType", "UserEmailUpdate");  // Thêm eventType
            jsonNode.set("data", objectMapper.valueToTree(event));
            // Thêm eventType vào JSON
            String json = objectMapper.writeValueAsString(jsonNode);
            System.out.println("Sending JSON to Kafka: " + json);

            // Gửi JSON String đến Kafka
            kafkaTemplate.send("auth.topic", json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
