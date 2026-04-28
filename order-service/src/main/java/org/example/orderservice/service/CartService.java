package org.example.orderservice.service;

import org.example.orderservice.dto.CartDto;
import org.example.orderservice.dto.CartItemDto;

import java.util.UUID;

public interface CartService {
    CartDto getCart(UUID userId);
    CartDto addItemToCart(UUID userId, CartItemDto item);
    CartDto removeItemFromCart(UUID userId, String productId);
    void clearCart(UUID userId);
}
