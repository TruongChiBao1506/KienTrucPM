package iuh.fit.se.productservice.controllers;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rate-limiter")
public class RateLimiterController {

    @Autowired
    private RateLimiterRegistry rateLimiterRegistry;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getRateLimiterStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // Collect all rate limiter metrics
        Map<String, Map<String, Object>> limiters = rateLimiterRegistry.getAllRateLimiters().stream()
            .collect(Collectors.toMap(
                RateLimiter::getName,
                limiter -> {
                    Map<String, Object> metrics = new HashMap<>();
                    metrics.put("availablePermissions", limiter.getMetrics().getAvailablePermissions());
                    metrics.put("numberOfWaitingThreads", limiter.getMetrics().getNumberOfWaitingThreads());
                    
                    Map<String, Object> config = new HashMap<>();
                    config.put("limitForPeriod", limiter.getRateLimiterConfig().getLimitForPeriod());
                    config.put("limitRefreshPeriod", limiter.getRateLimiterConfig().getLimitRefreshPeriod().toString());
                    config.put("timeoutDuration", limiter.getRateLimiterConfig().getTimeoutDuration().toString());
                    
                    metrics.put("config", config);
                    return metrics;
                }
            ));
        
        status.put("rateLimiters", limiters);
        return ResponseEntity.ok(status);
    }
}
