package iuh.fit.se.apigateway.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        //Chỉ định các nguồn hợp lệ
        corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:8080"));
        corsConfig.setAllowCredentials(true); // Cho phép gửi credentials (cookies, authorization headers)

        //Cho phép tất cả phương thức HTTP
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        //Cho phép tất cả headers
        corsConfig.setAllowedHeaders(Arrays.asList("*"));

        //Expose các headers quan trọng (nếu có JWT hoặc authentication)
        corsConfig.addExposedHeader("Authorization");

        //Giữ cấu hình trong 1 giờ
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
