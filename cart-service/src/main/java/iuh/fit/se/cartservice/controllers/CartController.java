package iuh.fit.se.cartservice.controllers;

import iuh.fit.se.cartservice.entities.Cart;
import iuh.fit.se.cartservice.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/carts")
public class CartController {
    @Autowired
    private CartService cartService;

    @PostMapping("/{userId}/add")
    public ResponseEntity<Map<String, Object>> addToCart(@PathVariable Long userId,
                                                          @RequestParam Long productId,
                                                          @RequestParam int quantity) {
        cartService.addToCart(userId, productId, quantity);
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "✅ Sản phẩm đã được thêm vào giỏ hàng");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getCart(@PathVariable Long userId) {
        Cart cart = cartService.getCart(userId);

        // Kiểm tra nếu cart == null, tạo mới giỏ hàng trống để tránh null
        if (cart == null) {
            cart = new Cart(); // tạo giỏ hàng trống
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("data", cart);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{userId}/remove")
    public ResponseEntity<?> removeItem(@PathVariable Long userId,
                                        @RequestParam Long productId) {
        cartService.removeFromCart(userId, productId);
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "🗑️ Sản phẩm đã được xóa khỏi giỏ hàng");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<?> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "🧹 Giỏ hàng đã được xóa");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{userId}/update")
    public ResponseEntity<?> updateCart(@PathVariable Long userId,
                                         @RequestParam Long productId,
                                         @RequestParam int quantity) {
        cartService.updateCart(userId, productId, quantity);
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "✅ Sản phẩm đã được cập nhật trong giỏ hàng");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
