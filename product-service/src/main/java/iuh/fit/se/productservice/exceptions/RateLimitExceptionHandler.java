package iuh.fit.se.productservice.exceptions;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class RateLimitExceptionHandler {

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<Map<String, Object>> handleRateLimitException(RequestNotPermitted exception) {
        Map<String, Object> response = new LinkedHashMap<>();
        
        response.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        response.put("error", "Rate Limit Exceeded");
        response.put("message", "Too many requests. Please try again later.");
        response.put("timestamp", LocalDateTime.now().toString());
        
        // Extract information about which rate limiter was triggered
        String rateLimiterName = extractRateLimiterName(exception);
        if (rateLimiterName != null) {
            response.put("limitType", rateLimiterName);
            
            // Add human-readable advice based on the rate limiter type
            switch (rateLimiterName) {
                case "searchEndpoints":
                    response.put("advice", "Please reduce the frequency of search operations.");
                    break;
                case "listingEndpoints":
                    response.put("advice", "Listing operations are rate limited. Try browsing fewer pages at once.");
                    break;
                case "writeEndpoints":
                    response.put("advice", "Write operations are heavily rate limited. Please space out your requests.");
                    break;
                case "detailEndpoints":
                    response.put("advice", "Detail view operations are rate limited. Try viewing fewer products at once.");
                    break;
                case "supportingEndpoints":
                    response.put("advice", "Supporting data requests are rate limited. Cache the data on client side when possible.");
                    break;
                default:
                    response.put("advice", "Please reduce your request rate to our API.");
            }
        }
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }
    
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleCustomRateLimitException(RateLimitExceededException exception) {
        Map<String, Object> response = new LinkedHashMap<>();
        
        response.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        response.put("error", "Rate Limit Exceeded");
        response.put("message", exception.getMessage());
        response.put("limitType", exception.getLimitType());
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }
    
    private String extractRateLimiterName(RequestNotPermitted exception) {
        // Extract limiter name from the exception message
        // Format is typically: "RateLimiter 'limitername' does not permit further calls"
        String message = exception.getMessage();
        if (message != null && message.contains("'")) {
            int start = message.indexOf("'") + 1;
            int end = message.indexOf("'", start);
            if (end > start) {
                return message.substring(start, end);
            }
        }
        return null;
    }
}
