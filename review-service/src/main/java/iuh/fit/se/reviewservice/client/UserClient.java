package iuh.fit.se.reviewservice.client;

import iuh.fit.se.reviewservice.configs.FeignHeaderInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import iuh.fit.se.reviewservice.dto.UserDto;

import java.util.Map;

@FeignClient(name = "user-service", path = "/api/users", configuration = FeignHeaderInterceptor.class)
public interface UserClient {
    @GetMapping("/{username}")
    ResponseEntity<Map<String, Object>> getUserByUsername(@PathVariable String username);
}