package iuh.fit.se.orderservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "auth-service", path = "/api/auth")
public interface AuthServiceClient {
    @GetMapping("/user-email/{id}")
    ResponseEntity<Map<String, Object>> getAuthUserEmailById(@PathVariable("id") Long id);
}
