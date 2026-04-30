package org.example.orderservice.service;

import org.example.orderservice.dto.CartDto;
import org.example.orderservice.dto.CartItemDto;

import java.util.UUID;

public interface CartService {
    CartDto getCart();
    CartDto addItemToCart(CartItemDto item);
    CartDto updateItemInCart(String productId, Integer quantity);
    CartDto removeItemFromCart(String productId);
    void clearCart();
}
