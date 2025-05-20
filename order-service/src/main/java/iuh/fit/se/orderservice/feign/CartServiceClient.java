package iuh.fit.se.orderservice.feign;

import iuh.fit.se.orderservice.configs.FeignConfig;
import iuh.fit.se.orderservice.configs.FeignHeaderInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cart-service", path = "/api/carts", configuration = {FeignHeaderInterceptor.class, FeignConfig.class})
public interface CartServiceClient {
    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<?> clearCart(@PathVariable Long userId);
}
