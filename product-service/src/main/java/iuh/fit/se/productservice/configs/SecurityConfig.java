package iuh.fit.se.productservice.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf->csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/products/glasses/*/update-stock**",
                                "/api/products/glasses",
                                "/api/products/glasses/**",
                                "/api/products/eyeglasses/men**",
                                "/api/products/eyeglasses/men",
                                "/api/products/eyeglasses/women**",
                                "/api/products/eyeglasses/women",
                                "/api/products/sunglasses/men**",
                                "/api/products/sunglasses/men",
                                "/api/products/sunglasses/women**",
                                "/api/products/sunglasses/women",
                                "/api/products/sunglasses",
                                "/api/products/eyeglasses",
                                "/api/products/brands",
                                "/api/products/shapes",
                                "/api/products/materials",
                                "/api/products/colors",
                                "/api/products/search**").permitAll()
                        .requestMatchers("/api/products/**").hasAnyRole("USER", "ADMIN", "SUPER")
                        .requestMatchers("/api/reviews/**").hasAnyRole("USER","ADMIN", "SUPER")
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(new HeaderAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Filter để xác thực thông tin từ header được gửi từ API Gateway
    public class HeaderAuthenticationFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {

            String path = request.getRequestURI();
            if (path != null && path.matches("^/api/products/glasses/\\d+/update-stock.*")) {
                filterChain.doFilter(request, response);
                return;
            }

            String username = request.getHeader("X-Auth-User");
            String rolesStr = request.getHeader("X-Auth-Roles");

            if (username != null && rolesStr != null) {
                List<SimpleGrantedAuthority> authorities = Arrays.stream(rolesStr.split(","))
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        }
    }
}
