package iuh.fit.se.reviewservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import iuh.fit.se.reviewservice.dto.ProductDto;

import java.util.Map;

@FeignClient(name = "product-service", path = "/api/products", configuration = CustomFeignClient.class)
public interface ProductClient {
    @GetMapping("/glasses/{id}")
    Map<String, Object> getProductById(@PathVariable("id") Long id);
}