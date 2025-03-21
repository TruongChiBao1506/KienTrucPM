package iuh.fit.se.reviewservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import iuh.fit.se.reviewservice.dto.ProductDto;

@FeignClient(name = "PRODUCT-SERVICE")
public interface ProductClient {
    @GetMapping("/api/products/{productId}")
    ProductDto getProductById(@PathVariable Long productId);
}