package iuh.fit.se.authservice.events.publishers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import iuh.fit.se.authservice.events.dtos.UserEmailUpdateEvent;
import iuh.fit.se.authservice.events.dtos.UserProfileCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserEventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;  // Gửi dạng String
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishUserProfileCreated(UserProfileCreatedEvent event) {
        try {
            // Tạo JSON object và thêm eventType
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.put("eventType", "UserProfileCreated");  // Thêm eventType
            jsonNode.set("data", objectMapper.valueToTree(event)); // Đưa dữ liệu vào
            // Thêm eventType vào JSON
            String json = objectMapper.writeValueAsString(jsonNode);
            System.out.println("Sending JSON to Kafka: " + json);

            // Gửi JSON String đến Kafka
            kafkaTemplate.send("user.topic", json);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo JSON", e);
        }
    }
    public void publishUserEmailResponse(UserEmailUpdateEvent event){
        try {
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.put("eventType", "UserEmailResponse");
            jsonNode.set("data", objectMapper.valueToTree(event));
            String json = objectMapper.writeValueAsString(jsonNode);
            System.out.println("Sending JSON to Kafka: " + json);

            kafkaTemplate.send("user.topic", json);
        }
        catch (Exception e){
            throw new RuntimeException("Lỗi khi tạo JSON", e);
        }
    }
    public void publishUserAsyncElasticsearch(Long id){
        try {
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.put("eventType", "AsyncElasticsearch");
            jsonNode.put("userId", id);
            String json = objectMapper.writeValueAsString(jsonNode);
            System.out.println("Sending JSON to Kafka: " + json);

            kafkaTemplate.send("user.topic", json);
        }
        catch (Exception e){
            throw new RuntimeException("Lỗi khi tạo JSON", e);
        }
    }
}
