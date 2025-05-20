package iuh.fit.se.chatbotservice.service;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import iuh.fit.se.chatbotservice.exception.RateLimitExceededException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for tracking and enforcing rate limits.
 * This implementation uses both Resilience4j for external API endpoints 
 * and a custom in-memory tracking for more granular user-based limits.
 */
@Service
public class RateLimitService {

    // Default limit based on Gemini API (15 requests per minute for Gemini 2.0 Flash)
    private static final int MAX_REQUESTS_PER_MINUTE = 15;
    
    // In-memory storage for request counts
    private final Map<String, AtomicInteger> requestCountsPerMinute = new ConcurrentHashMap<>();
    private final Map<String, Long> resetTimeMap = new ConcurrentHashMap<>();
    
    private final RateLimiterRegistry rateLimiterRegistry;

    public RateLimitService(RateLimiterRegistry rateLimiterRegistry) {
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    /**
     * Try to consume a rate limit for a specific user or API key.
     * This method provides more granular control beyond the controller-level
     * rate limiting by checking individual user limits.
     * 
     * @param key User ID or API key to track
     * @return true if the request can proceed, false if rate limited
     */
    public boolean tryConsume(String key) {
        long currentTimeMillis = System.currentTimeMillis();
        long currentMinute = currentTimeMillis / 60000;

        // Create key for current minute
        String minuteKey = key + ":" + currentMinute;

        // Check and reset if it's a new minute
        resetTimeMap.computeIfAbsent(key, k -> currentMinute);
        if (resetTimeMap.get(key) < currentMinute) {
            resetTimeMap.put(key, currentMinute);
            requestCountsPerMinute.remove(minuteKey);
        }

        // Increment counter
        AtomicInteger counter = requestCountsPerMinute.computeIfAbsent(minuteKey, k -> new AtomicInteger(0));
        int currentCount = counter.incrementAndGet();

        // Check if over limit
        if (currentCount > MAX_REQUESTS_PER_MINUTE) {
            counter.decrementAndGet();
            return false;
        }

        return true;
    }
    
    /**
     * Try to consume a rate limit with a custom rate limiter from Resilience4j.
     * Throws RateLimitExceededException if the rate limit is exceeded.
     * 
     * @param key User ID or API key
     * @param rateLimiterName Name of the Resilience4j rate limiter to use
     * @throws RateLimitExceededException If rate limit is exceeded
     */
    public void tryConsumeOrThrow(String key, String rateLimiterName) {
        if (!tryConsume(key)) {
            throw new RateLimitExceededException("Rate limit exceeded for key: " + key);
        }
        
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(rateLimiterName);
        rateLimiter.acquirePermission();
    }
    
    /**
     * Get remaining permits for a user or API key
     * 
     * @param key User ID or API key
     * @return Number of remaining requests allowed in the current minute
     */
    public int getRemainingPermits(String key) {
        long currentMinute = System.currentTimeMillis() / 60000;
        String minuteKey = key + ":" + currentMinute;
        
        AtomicInteger counter = requestCountsPerMinute.get(minuteKey);
        if (counter == null) {
            return MAX_REQUESTS_PER_MINUTE;
        }
        
        int remaining = MAX_REQUESTS_PER_MINUTE - counter.get();
        return Math.max(0, remaining);
    }
}