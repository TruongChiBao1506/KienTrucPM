package iuh.fit.se.reviewservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import iuh.fit.se.reviewservice.dto.UserDto;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/api/users/{username}")
    UserDto getUserByUsername(@PathVariable String username);
}