package iuh.fit.se.chatbotservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "product-service")
public interface ProductServiceClient {

    @GetMapping("/api/products/glasses")
    ResponseEntity<Map<String, Object>> getAllGlasses();

    @GetMapping("/api/products/glasses/{id}")
    ResponseEntity<Map<String, Object>> getGlassById(@PathVariable("id") Long id);

    @GetMapping("/api/products/eyeglasses")
    ResponseEntity<Map<String, Object>> getAllEyeglasses();

    @GetMapping("/api/products/sunglasses")
    ResponseEntity<Map<String, Object>> getAllSunglasses();

    @GetMapping("/api/products/eyeglasses/men")
    ResponseEntity<Map<String, Object>> getMenEyeglasses(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String shape,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice);

    @GetMapping("/api/products/eyeglasses/women")
    ResponseEntity<Map<String, Object>> getWomenEyeglasses(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String shape,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice);

    @GetMapping("/api/products/sunglasses/men")
    ResponseEntity<Map<String, Object>> getMenSunglasses(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String shape,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice);

    @GetMapping("/api/products/sunglasses/women")
    ResponseEntity<Map<String, Object>> getWomenSunglasses(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String shape,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice);

    @GetMapping("/api/products/brands")
    ResponseEntity<Map<String, Object>> getAllBrands();

    @GetMapping("/api/products/shapes")
    ResponseEntity<Map<String, Object>> getAllShapes();

    @GetMapping("/api/products/materials")
    ResponseEntity<Map<String, Object>> getAllMaterials();

    @GetMapping("/api/products/colors")
    ResponseEntity<Map<String, Object>> getAllColors();

    @GetMapping("/api/products/search")
    ResponseEntity<Map<String, Object>> searchProducts(@RequestParam("keyword") String keyword);
}