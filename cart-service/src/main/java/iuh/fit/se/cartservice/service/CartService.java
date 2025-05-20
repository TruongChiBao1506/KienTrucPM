package iuh.fit.se.cartservice.service;

import iuh.fit.se.cartservice.entities.Cart;

public interface CartService {
    public Cart getCart(Long userId);
    public void addToCart(Long userId, Long productId, int quantity);
    public void removeFromCart(Long userId, Long productId);
    public void clearCart(Long userId);
    public void updateCart(Long userId, Long productId, int quantity);
}
