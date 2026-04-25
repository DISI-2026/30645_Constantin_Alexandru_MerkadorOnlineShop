package org.example.orderservice.service;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.dto.CartDto;
import org.example.orderservice.dto.CartItemDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CART_KEY_PREFIX = "cart:";
    private static final long CART_EXPIRATION_DAYS = 7;

    @Override
    public CartDto getCart(UUID userId) {
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
    public CartDto addItemToCart(UUID userId, CartItemDto newItem) {
        String key = CART_KEY_PREFIX + userId.toString();
        CartDto cart = getCart(userId);

        Optional<CartItemDto> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(newItem.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + newItem.getQuantity());
        } else {
            cart.getItems().add(newItem);
        }

        updateCartTotal(cart);
        redisTemplate.opsForValue().set(key, cart, CART_EXPIRATION_DAYS, TimeUnit.DAYS);
        return cart;
    }

    @Override
    public CartDto removeItemFromCart(UUID userId, String productId) {
        String key = CART_KEY_PREFIX + userId.toString();
        CartDto cart = getCart(userId);

        cart.getItems().removeIf(item -> item.getProductId().equals(productId));

        updateCartTotal(cart);
        redisTemplate.opsForValue().set(key, cart, CART_EXPIRATION_DAYS, TimeUnit.DAYS);
        return cart;
    }

    @Override
    public void clearCart(UUID userId) {
        String key = CART_KEY_PREFIX + userId.toString();
        redisTemplate.delete(key);
    }

    private void updateCartTotal(CartDto cart) {
        BigDecimal total = cart.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotal(total);
    }
}
