package iuh.fit.se.cartservice.service.impl;

import iuh.fit.se.cartservice.entities.Cart;
import iuh.fit.se.cartservice.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate<String, Cart> redisTemplate;

    private String getCartKey(Long userId) {
        return "cart:" + userId;
    }

    @Override
    public Cart getCart(Long userId) {
        Cart cart = redisTemplate.opsForValue().get(getCartKey(userId));

        // Nếu không có giỏ hàng trong Redis, trả về giỏ hàng trống
        if (cart == null) {
            cart = new Cart();
        }

        return cart;
    }

    @Override
    public void addToCart(Long userId, Long productId, int quantity) {
        Cart cart = getCart(userId);
        if (cart == null) {
            cart = new Cart();
        }
        cart.addItem(productId, quantity); // ✅ cộng dồn số lượng
        redisTemplate.opsForValue().set(getCartKey(userId), cart);
    }

    @Override
    public void removeFromCart(Long userId, Long productId) {
        Cart cart = getCart(userId);
        if (cart != null) {
            cart.removeItem(productId);
            redisTemplate.opsForValue().set(getCartKey(userId), cart);
        }
    }

    @Override
    public void clearCart(Long userId) {
        redisTemplate.delete(getCartKey(userId));
    }

    @Override
    public void updateCart(Long userId, Long productId, int quantity) {
        Cart cart = getCart(userId);
        if (cart != null) {
            cart.updateItem(productId, quantity);
            redisTemplate.opsForValue().set(getCartKey(userId), cart);
        }
    }
}
