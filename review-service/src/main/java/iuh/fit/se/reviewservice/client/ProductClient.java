package iuh.fit.se.reviewservice.client;

import iuh.fit.se.reviewservice.configs.FeignHeaderInterceptor;
import iuh.fit.se.reviewservice.configs.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import iuh.fit.se.reviewservice.dto.ProductDto;

import java.util.Map;

@FeignClient(name = "product-service", path = "/api/products", configuration = {FeignHeaderInterceptor.class, FeignConfig.class})
public interface ProductClient {
    @GetMapping("/glasses-review/{id}")
    ResponseEntity<Map<String, Object>> getProductById(@PathVariable Long id);
}