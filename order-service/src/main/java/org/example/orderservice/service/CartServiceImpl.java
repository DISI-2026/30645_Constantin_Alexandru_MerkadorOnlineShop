package org.example.orderservice.service;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.dto.CartDto;
import org.example.orderservice.dto.CartItemDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CART_KEY_PREFIX = "cart:";
    private static final long CART_EXPIRATION_DAYS = 7;

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("User is not authenticated.");
        }
        return UUID.fromString(authentication.getName());
    }

    @Override
    public CartDto getCart() {
        UUID userId = getCurrentUserId();
        String key = CART_KEY_PREFIX + userId.toString();
        CartDto cart = (CartDto) redisTemplate.opsForValue().get(key);

        if (cart == null) {
            return new CartDto(userId, new ArrayList<>(), BigDecimal.ZERO);
        }
        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }
        return cart;
    }

    @Override
    public CartDto addItemToCart(CartItemDto newItem) {
        UUID userId = getCurrentUserId();
        String key = CART_KEY_PREFIX + userId.toString();
        CartDto cart = getCart();

        cart.getItems().stream()
                .filter(item -> item.getProductId().equals(newItem.getProductId()))
                .findFirst()
                .ifPresentOrElse(
                        existingItem -> existingItem.setQuantity(existingItem.getQuantity() + newItem.getQuantity()),
                        () -> cart.getItems().add(newItem)
                );

        return saveCart(key, cart);
    }

    @Override
    public CartDto updateItemInCart(String productId, Integer quantity) {
        UUID userId = getCurrentUserId();
        String key = CART_KEY_PREFIX + userId.toString();
        CartDto cart = getCart();

        cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));
        
        cart.getItems().removeIf(item -> item.getQuantity() <= 0);

        return saveCart(key, cart);
    }

    @Override
    public CartDto removeItemFromCart(String productId) {
        UUID userId = getCurrentUserId();
        String key = CART_KEY_PREFIX + userId.toString();
        CartDto cart = getCart();
        cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        return saveCart(key, cart);
    }

    @Override
    public void clearCart() {
        UUID userId = getCurrentUserId();
        String key = CART_KEY_PREFIX + userId.toString();
        redisTemplate.delete(key);
    }

    private CartDto saveCart(String key, CartDto cart) {
        updateCartTotal(cart);
        redisTemplate.opsForValue().set(key, cart, CART_EXPIRATION_DAYS, TimeUnit.DAYS);
        return cart;
    }

    private void updateCartTotal(CartDto cart) {
        BigDecimal total = cart.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotal(total);
    }
}
