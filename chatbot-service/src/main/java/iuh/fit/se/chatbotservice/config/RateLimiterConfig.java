package iuh.fit.se.chatbotservice.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Resilience4j Rate Limiter.
 * This class registers and configures rate limiters for different endpoints.
 */
@Configuration
@RequiredArgsConstructor
public class RateLimiterConfig {

    private final RateLimiterRegistry rateLimiterRegistry;

    /**
     * Bean for the chat endpoint rate limiter.
     * This limits API calls to Gemini API to protect from costs and API rate limits.
     */
    @Bean
    public RateLimiter chatEndpointRateLimiter() {
        return rateLimiterRegistry.rateLimiter("chatEndpoint");
    }

    /**
     * Bean for the navigation endpoint rate limiter.
     */
    @Bean
    public RateLimiter navigationEndpointRateLimiter() {
        return rateLimiterRegistry.rateLimiter("navigationEndpoint");
    }

    /**
     * Bean for the conversation management endpoints rate limiter.
     */
    @Bean
    public RateLimiter conversationEndpointRateLimiter() {
        return rateLimiterRegistry.rateLimiter("conversationEndpoint");
    }
}
