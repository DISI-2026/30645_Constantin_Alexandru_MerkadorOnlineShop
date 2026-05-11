package org.example.orderservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.dto.OrderResponseDto;
import org.example.orderservice.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/seller/orders")
@RequiredArgsConstructor
public class SellerOrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getOrdersForSeller() {
        return ResponseEntity.ok(orderService.getOrdersForSeller());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable("id") UUID orderId,
            @RequestBody org.example.orderservice.dto.UpdateOrderStatusRequestDto statusRequest) {
        OrderResponseDto updatedOrder = orderService.updateOrderStatus(orderId, statusRequest.getStatus());
        return ResponseEntity.ok(updatedOrder);
    }
}
