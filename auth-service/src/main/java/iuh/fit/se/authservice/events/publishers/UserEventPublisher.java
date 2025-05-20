package iuh.fit.se.authservice.events.publishers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import iuh.fit.se.authservice.dtos.OtpEmailEvent;
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }
    public void publishSendOtpEamil(OtpEmailEvent event){
        try {
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.put("eventType", "SendOtpEmail");
            jsonNode.set("data", objectMapper.valueToTree(event));
            String json = objectMapper.writeValueAsString(jsonNode);
            System.out.println("Sending JSON to Kafka: " + json);

            kafkaTemplate.send("email.topic", json);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public void sendResetPasswordEmail(String email, String token){
        try {
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.put("eventType", "SendResetPasswordEmail");
            jsonNode.set("email", objectMapper.valueToTree(email));
            jsonNode.set("token", objectMapper.valueToTree(token));
            String json = objectMapper.writeValueAsString(jsonNode);
            System.out.println("Sending JSON to Kafka: " + json);

            kafkaTemplate.send("email.topic", json);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
