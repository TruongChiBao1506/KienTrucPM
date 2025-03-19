package iuh.fit.se.userservice.feign;

import iuh.fit.se.userservice.dtos.AuthUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "auth-service", path = "/api/auth")
public interface AuthServiceClient {
    @GetMapping("/all-by-role/{role}")
    List<Long>getUserIdsByRole(@PathVariable("role") String role);
    @GetMapping("/role/{userId}")
    String getRoleByUserId(@PathVariable("userId") Long userId);
    @DeleteMapping("/delete/{id}")
    ResponseEntity<Map<String, Object>> deleteAuthUser(@PathVariable("id") Long id);
    @PostMapping("/add")
    ResponseEntity<Map<String, Object>> addAuthUser(@RequestBody AuthUser authUser);
    @PostMapping("/update")
    ResponseEntity<Map<String, Object>> updateAuthUser(@RequestBody AuthUser authUser);
    @GetMapping("/user/{id}")
    ResponseEntity<Map<String, Object>> getAuthUser(@PathVariable("id") Long id);
}
