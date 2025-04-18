package iuh.fit.se.apigateway.configs;

import iuh.fit.se.apigateway.filter.AuthenticationFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    private final AuthenticationFilter authenticationFilter;

    public GatewayConfig(AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/api/auth/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config()))) // Áp dụng filter
                        .uri("lb://AUTH-SERVICE"))
                .route("user-service", r -> r.path("/api/users/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config()))) // Áp dụng filter
                        .uri("lb://USER-SERVICE"))
                .route("product-service", r -> r.path("/api/products/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config()))) // Áp dụng filter
                        .uri("lb://PRODUCT-SERVICE"))
                .route("order-service", r -> r.path("/api/orders/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config()))) // Áp dụng filter
                        .uri("lb://ORDER-SERVICE"))
                .route("review-service", r -> r.path("/api/reviews/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config()))) // Áp dụng filter
                        .uri("lb://REVIEW-SERVICE"))
                .route("review-service-es", r -> r.path("/api/es/test/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config()))) // Áp dụng filter
                        .uri("lb://REVIEW-SERVICE"))
                .route("notification-service", r -> r.path("/api/notifications/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config()))) // Áp dụng filter
                        .uri("lb://NOTIFICATION-SERVICE"))
                .route("email-service", r -> r.path("/api/send-email/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config()))) // Áp dụng filter
                        .uri("lb://EMAIL-SERVICE"))
                .build();

    }
}
