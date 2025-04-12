package iuh.fit.se.reviewservice.client;

import iuh.fit.se.reviewservice.configs.FeignHeaderInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import iuh.fit.se.reviewservice.dto.ProductDto;

@FeignClient(name = "product-service", path = "/api/products", configuration = FeignHeaderInterceptor.class)
public interface ProductClient {
    @GetMapping("/{productId}")
    ProductDto getProductById(@PathVariable Long productId);
}