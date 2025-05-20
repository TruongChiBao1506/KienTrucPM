package iuh.fit.se.chatbotservice.exception;

/**
 * Custom exception for rate limit exceeded scenarios.
 * Used for cases when the custom RateLimitService detects a rate limit violation.
 */
public class RateLimitExceededException extends RuntimeException {
    
    public RateLimitExceededException(String message) {
        super(message);
    }
    
    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
