package iuh.fit.se.orderservice.feign;

import iuh.fit.se.orderservice.configs.FeignHeaderInterceptor;
import iuh.fit.se.orderservice.configs.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "user-service", path = "/api/users", configuration = {FeignHeaderInterceptor.class, FeignConfig.class})
public interface UserServiceClient {
    @GetMapping("/user-profile/{id}")
    ResponseEntity<Map<String, Object>> getUserProfileById (@PathVariable Long id);
}
