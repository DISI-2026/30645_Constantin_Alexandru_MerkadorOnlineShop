package org.example.orderservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.dto.CartDto;
import org.example.orderservice.dto.CartItemDto;
import org.example.orderservice.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<CartDto> getCart(@PathVariable UUID userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/{userId}/items")
    public ResponseEntity<CartDto> addItemToCart(@PathVariable UUID userId, @RequestBody CartItemDto item) {
        return ResponseEntity.ok(cartService.addItemToCart(userId, item));
    }

    @DeleteMapping("/{userId}/items/{productId}")
    public ResponseEntity<CartDto> removeItemFromCart(@PathVariable UUID userId, @PathVariable String productId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(userId, productId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable UUID userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
