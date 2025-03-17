package iuh.fit.se.userservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "auth-service", path = "/api/auth")
public interface AuthServiceClient {
    @GetMapping("/all-by-role/{role}")
    List<Long>getUserIdsByRole(@PathVariable("role") String role);
    @GetMapping("/role/{userId}")
    String getRoleByUserId(@PathVariable("userId") Long userId);
}
