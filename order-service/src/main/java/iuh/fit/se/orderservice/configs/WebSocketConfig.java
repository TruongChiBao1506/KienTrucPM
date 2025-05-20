package iuh.fit.se.orderservice.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Kênh dùng để gửi thông điệp đến client
        config.enableSimpleBroker("/topic");
        // Tiền tố cho các yêu cầu gửi từ client lên server
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint cho client kết nối
        registry.addEndpoint("/ws").setAllowedOrigins("http://localhost:8889").withSockJS();
    }
}
