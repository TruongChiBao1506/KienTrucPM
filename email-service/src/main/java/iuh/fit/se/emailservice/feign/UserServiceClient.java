package iuh.fit.se.emailservice.feign;

import iuh.fit.se.emailservice.configs.FeignHeaderInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "user-service", path = "/api/users", configuration = FeignHeaderInterceptor.class)
public interface UserServiceClient {
    @GetMapping("/user-profile/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id);
}
