package iuh.fit.se.productservice.configs;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimiterConfig {

    @Autowired
    private RateLimiterProperties properties;

    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        return RateLimiterRegistry.ofDefaults();
    }
    
    @Bean
    public RateLimiter searchEndpointsRateLimiter(RateLimiterRegistry registry) {
        return registry.rateLimiter("searchEndpoints");
    }
    
    @Bean
    public RateLimiter listingEndpointsRateLimiter(RateLimiterRegistry registry) {
        return registry.rateLimiter("listingEndpoints");
    }
    
    @Bean
    public RateLimiter writeEndpointsRateLimiter(RateLimiterRegistry registry) {
        return registry.rateLimiter("writeEndpoints");
    }
    
    @Bean
    public RateLimiter detailEndpointsRateLimiter(RateLimiterRegistry registry) {
        return registry.rateLimiter("detailEndpoints");
    }
    
    @Bean
    public RateLimiter supportingEndpointsRateLimiter(RateLimiterRegistry registry) {
        return registry.rateLimiter("supportingEndpoints");
    }
}
