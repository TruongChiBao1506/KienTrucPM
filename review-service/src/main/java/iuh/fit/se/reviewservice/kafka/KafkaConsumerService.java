package iuh.fit.se.reviewservice.kafka;

import iuh.fit.se.reviewservice.configs.KafkaConfig;
import iuh.fit.se.reviewservice.dto.ProductDto;
import iuh.fit.se.reviewservice.dto.UserDto;
import iuh.fit.se.reviewservice.events.ProductInfoEvent;
import iuh.fit.se.reviewservice.events.UserInfoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    // In-memory cache for user and product information
    private final Map<String, UserDto> userCache = new ConcurrentHashMap<>();
    private final Map<Long, ProductDto> productCache = new ConcurrentHashMap<>();

    @KafkaListener(topics = KafkaConfig.USER_INFO_TOPIC, groupId = "${spring.kafka.consumer.group-id:review-service-group}")
    public void consumeUserInfo(UserInfoEvent event) {
        try {
            log.info("Received user info event from Kafka: {}", event);
            
            UserDto userDto = new UserDto();
            userDto.setId(event.getId());
            userDto.setUsername(event.getUsername());
            userDto.setEmail(event.getEmail());
            
            // Cache the user information
            userCache.put(event.getUsername(), userDto);
            
            log.info("User info processed and cached successfully");
        } catch (Exception e) {
            log.error("Error processing user info event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = KafkaConfig.PRODUCT_INFO_TOPIC, groupId = "${spring.kafka.consumer.group-id:review-service-group}")
    public void consumeProductInfo(ProductInfoEvent event) {
        try {
            log.info("Received product info event from Kafka: {}", event);
            
            ProductDto productDto = new ProductDto();
            productDto.setId(event.getId());
            productDto.setName(event.getName());
            productDto.setDescription(event.getDescription());
            productDto.setPrice(event.getPrice());
            
            // Cache the product information
            productCache.put(event.getId(), productDto);
            
            log.info("Product info processed and cached successfully");
        } catch (Exception e) {
            log.error("Error processing product info event: {}", e.getMessage(), e);
        }
    }

    // Methods to retrieve cached information
    public UserDto getUserByUsername(String username) {
        return userCache.get(username);
    }

    public ProductDto getProductById(Long productId) {
        return productCache.get(productId);
    }
}