package iuh.fit.se.orderservice.feign;

import iuh.fit.se.orderservice.configs.FeignHeaderInterceptor;
import iuh.fit.se.orderservice.configs.FeignConfig;
import iuh.fit.se.orderservice.dtos.OrderItemFromProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "product-service", path = "/api/products", configuration = {FeignHeaderInterceptor.class, FeignConfig.class})
public interface ProductServiceClient {
    @PostMapping("/glasses/{id}/update-stock")
    ResponseEntity<Map<String, Object>> updateStockProduct(@PathVariable Long id, @RequestParam int quantity);
    @GetMapping("/glassesDTO/{id}")
    OrderItemFromProductDTO getProductById(@PathVariable Long id);


}
