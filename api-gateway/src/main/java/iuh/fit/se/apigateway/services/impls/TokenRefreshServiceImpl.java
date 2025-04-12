package iuh.fit.se.apigateway.services.impls;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.se.apigateway.dtos.AuthResponse;
import iuh.fit.se.apigateway.dtos.RefreshRequest;
import iuh.fit.se.apigateway.services.TokenRefreshService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class TokenRefreshServiceImpl implements TokenRefreshService {
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public TokenRefreshServiceImpl(WebClient.Builder webClientBuilder, @Value("${auth.service.url:http://localhost:8081}") String authServiceUrl, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl(authServiceUrl).build();
        this.objectMapper = objectMapper;
    }

    public Mono<String> getRefreshToken(String username) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/auth/get-refresh-token")
                        .queryParam("username", username)
                        .build())
                .retrieve()
                .onStatus(status -> status.isError(), response -> response.bodyToMono(String.class)
                        .flatMap(error -> Mono.error(new RuntimeException("Failed to get refresh token: " + error))))
                .bodyToMono(String.class)
                .map(json -> {
                    try {
                        JsonNode node = objectMapper.readTree(json);
                        return node.get("refreshToken").asText();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse refresh token from JSON: " + json, e);
                    }
                })
                .doOnSuccess(token -> System.out.println("Got refresh token: " + token))
                .doOnError(e -> System.err.println("Error getting refresh token: " + e.getMessage()));

    }

    public Mono<AuthResponse> refreshToken(String refreshToken) {
        RefreshRequest request = new RefreshRequest(refreshToken);
        System.out.println("Sending payload: " + request);
        return webClient.post()
                .uri("/api/auth/refresh")
                .header("Content-Type", "application/json")
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.isError(), response -> response.bodyToMono(String.class)
                        .flatMap(error -> Mono.error(new RuntimeException("Failed to refresh token: " + response.statusCode() + ", body: " + error))))
                .bodyToMono(AuthResponse.class);
    }
}
