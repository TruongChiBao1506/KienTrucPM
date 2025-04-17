package iuh.fit.se.reviewservice.kafka;

import iuh.fit.se.reviewservice.configs.KafkaConfig;
import iuh.fit.se.reviewservice.events.ReviewCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendReviewCreatedEvent(ReviewCreatedEvent event) {
        try {
            log.info("Sending review created event to Kafka: {}", event);
            kafkaTemplate.send(KafkaConfig.REVIEW_CREATED_TOPIC, event);
            log.info("Review created event sent successfully");
        } catch (Exception e) {
            log.error("Error sending review created event to Kafka: {}", e.getMessage(), e);
        }
    }
}