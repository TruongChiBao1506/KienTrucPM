package iuh.fit.se.orderservice.events.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import iuh.fit.se.orderservice.dtos.Notification;
import iuh.fit.se.orderservice.entities.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    public OrderEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    public void sendNotification(Notification notification) {
        try {
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.put("eventType", "sendNotification");
            jsonNode.set("data", objectMapper.valueToTree(notification));

            String json = objectMapper.writeValueAsString(jsonNode);
            System.out.println("Sending JSON to Kafka: " + json);

            // Gửi JSON String đến Kafka
            kafkaTemplate.send("notification.topic", json);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendEmail(Order order, String email, StringBuilder productTable) {
        try {
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.put("eventType", "sendEmail");
            jsonNode.put("order", objectMapper.valueToTree(order));
            jsonNode.put("email", email);
            jsonNode.put("productTable", productTable.toString());
            String json = objectMapper.writeValueAsString(jsonNode);
            System.out.println("Sending JSON to Kafka: " + json);
            // Gửi JSON String đến Kafka
            kafkaTemplate.send("email.topic", json);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
