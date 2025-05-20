package iuh.fit.se.productservice.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "resilience4j.ratelimiter")
@Data
public class RateLimiterProperties {
    private RateLimiterConfig instances = new RateLimiterConfig();

    @Data
    public static class RateLimiterConfig {
        private RateLimiterInstanceProperties searchEndpoints = new RateLimiterInstanceProperties();
        private RateLimiterInstanceProperties listingEndpoints = new RateLimiterInstanceProperties();
        private RateLimiterInstanceProperties writeEndpoints = new RateLimiterInstanceProperties();
        private RateLimiterInstanceProperties detailEndpoints = new RateLimiterInstanceProperties();
        private RateLimiterInstanceProperties supportingEndpoints = new RateLimiterInstanceProperties();
    }

    @Data
    public static class RateLimiterInstanceProperties {
        private int limitForPeriod;
        private Duration limitRefreshPeriod;
        private Duration timeoutDuration;
        private boolean allowHealthIndicator;
    }
}
