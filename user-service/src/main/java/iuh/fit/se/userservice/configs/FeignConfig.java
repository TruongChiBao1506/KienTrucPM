package iuh.fit.se.userservice.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    
//    @Bean
//    public ErrorDecoder errorDecoder(ObjectMapper objectMapper) {
//        return new FeignErrorDecoder(objectMapper);
//    }
    @Bean
    public Request.Options options() {
        return new Request.Options(
                5_000, // Connect timeout: 5 giây
                10_000 // Read timeout: 10 giây
        );
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            if (response.status() >= 500) {
                return new RuntimeException("Server error from auth-service: " + response.status());
            }
            return new ErrorDecoder.Default().decode(methodKey, response);
        };
    }
}
