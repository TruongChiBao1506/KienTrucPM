package iuh.fit.se.apigateway.filter;

import iuh.fit.se.apigateway.services.TokenRefreshService;
import iuh.fit.se.apigateway.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenRefreshService tokenRefreshService;


    private static final List<String> OPEN_ENDPOINTS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/verify-otp",
            "/api/auth/refresh",
            "/api/auth/logout",
            "/api/auth/get-refresh-token",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/orders/vnpay-return",
            "/api/orders/vnpay-return/",
            "/swagger-ui",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/webjars/**",
            "/api/products/glasses",
            "/api/products/eyeglasses/men",
            "/api/products/eyeglasses/women",
            "/api/products/sunglasses/men",
            "/api/products/sunglasses/women",
            "/api/products/eyeglasses/men**",
            "/api/products/eyeglasses/women**",
            "/api/products/sunglasses/men**",
            "/api/products/sunglasses/women**",
            "/api/products/brands",
            "/api/products/shapes",
            "/api/products/materials",
            "/api/products/colors",
            "/api/products/search",
            "/ws/",
            "/ws",
            "/ws/info",
            "/api/chatbot/chat"
    );

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            System.out.println("Tiến hành xác thực JWT");
            String path = exchange.getRequest().getURI().getPath();

            // Bỏ qua kiểm tra JWT nếu endpoint nằm trong danh sách OPEN_ENDPOINTS
            for (String openEndpoint : OPEN_ENDPOINTS) {
                if (path.contains(openEndpoint)) {
                    System.out.println("Bỏ qua xác thực JWT cho endpoint: " + path);
                    return chain.filter(exchange);
                }
            }

            // Kiểm tra header Authorization
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "No Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid Authorization format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                if (jwtUtil.isTokenExpired(token)) {
                    System.out.println("Token expired, attempting to refresh");

                    // 👉 Lấy refresh token từ cookie
                    HttpCookie refreshCookie = exchange.getRequest().getCookies().getFirst("refreshToken");

                    if (refreshCookie == null || refreshCookie.getValue().isEmpty()) {
                        System.out.println("❌ Không tìm thấy refresh token trong cookie");
                        return onError(exchange, "Refresh token không tồn tại", HttpStatus.UNAUTHORIZED);
                    }

                    String refreshToken = refreshCookie.getValue();

                    // 👉 Gọi service để làm mới access token từ refresh token
                    return tokenRefreshService.refreshToken(refreshToken)
                            .flatMap(response -> {
                                String newAccessToken = response.getAccessToken();
                                if (newAccessToken == null || newAccessToken.isEmpty()) {
                                    System.out.println("❌ Access token rỗng sau khi refresh");
                                    return onError(exchange, "Không thể làm mới access token", HttpStatus.UNAUTHORIZED);
                                }

                                String newUsername = jwtUtil.extractUsername(newAccessToken);
                                List<String> roles = jwtUtil.extractRoles(newAccessToken);

                                ServerHttpRequest finalRequest = exchange.getRequest().mutate()
                                        // Không cần gắn lại access token nếu không dùng phía dưới
                                        .header("X-Auth-User", newUsername)
                                        .header("X-Auth-Roles", String.join(",", roles != null ? roles : List.of()))
                                        .build();

                                System.out.println("✅ Làm mới token thành công, tiếp tục request");
                                return chain.filter(exchange.mutate().request(finalRequest).build());
                            })
                            .onErrorResume(e -> {
                                e.printStackTrace(); // debug
                                return onError(exchange, "Làm mới token thất bại: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
                            });
                }


                String username = jwtUtil.extractUsername(token);
                List<String> roles = jwtUtil.extractRoles(token); // Lấy roles từ JWT
                // Forward thông tin user và roles đến backend services
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-Auth-User", username)
                        .header("X-Auth-Roles", String.join(",", roles))
                        .build();
                System.out.println("xác thực JWT thành công");
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (Exception e) {
                return onError(exchange, "Invalid token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    public static class Config {
        // Có thể thêm cấu hình tùy chỉnh ở đây nếu cần
    }
}
