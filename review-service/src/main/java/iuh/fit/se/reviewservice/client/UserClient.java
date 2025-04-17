package iuh.fit.se.reviewservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import iuh.fit.se.reviewservice.dto.UserDto;

import java.util.Map;

@FeignClient(name = "user-service", path = "/api/users", configuration = CustomFeignClient.class)
public interface UserClient {
    @GetMapping("/{username}")
    Map<String, Object> getUserByUsername(@PathVariable("username") String username);}