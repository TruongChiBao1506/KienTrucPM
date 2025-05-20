package iuh.fit.se.apigateway.services;

import iuh.fit.se.apigateway.dtos.AuthResponse;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface TokenRefreshService {
    public Mono<String> getRefreshToken(String username);
    public Mono<AuthResponse> refreshToken(String refreshToken);
}
