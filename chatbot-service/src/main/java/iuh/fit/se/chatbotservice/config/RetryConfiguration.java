package iuh.fit.se.chatbotservice.config;

import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Duration;

/**
 * Cấu hình Retry Pattern cho chatbot-service
 * Giúp service tự động thử lại khi gặp lỗi tạm thời khi gọi API bên ngoài
 */
@Configuration
public class RetryConfiguration {

    /**
     * Tạo và cấu hình RetryRegistry
     * @return RetryRegistry đã cấu hình
     */
    @Bean
    public RetryRegistry retryRegistry() {
        // Cấu hình retry cho các lời gọi API Gemini
        RetryConfig geminiApiRetryConfig = RetryConfig.custom()
                .maxAttempts(3)  // Tổng số lần thử tối đa là 3 (1 lần ban đầu + 2 lần thử lại)
                .waitDuration(Duration.ofMillis(500))  // Thời gian chờ giữa các lần thử là 500ms
                .retryExceptions(IOException.class, SocketTimeoutException.class, ResourceAccessException.class)
                .build();
        
        // Khởi tạo registry với cấu hình mặc định
        RetryRegistry registry = RetryRegistry.ofDefaults();
        
        // Đăng ký cấu hình cho geminiApiRetry
        registry.addConfiguration("geminiApiRetry", geminiApiRetryConfig);
        
        return registry;
    }
}
