package org.example.orderservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.dto.CartDto;
import org.example.orderservice.dto.CartItemDto;
import org.example.orderservice.dto.UpdateCartItemRequestDto;
import org.example.orderservice.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDto> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> addItemToCart(@RequestBody CartItemDto item) {
        return ResponseEntity.ok(cartService.addItemToCart(item));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartDto> updateItemInCart(@PathVariable String productId, @RequestBody UpdateCartItemRequestDto request) {
        return ResponseEntity.ok(cartService.updateItemInCart(productId, request.getQuantity()));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartDto> removeItemFromCart(@PathVariable String productId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(productId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
}
