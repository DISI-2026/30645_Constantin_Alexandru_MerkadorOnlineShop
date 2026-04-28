package org.example.orderservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.dto.OrderResponseDto;
import org.example.orderservice.dto.UpdateOrderStatusRequestDto;
import org.example.orderservice.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class OrderAdminController {

    private final OrderService orderService;

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable("id") UUID orderId,
            @RequestBody UpdateOrderStatusRequestDto statusRequest) {
        OrderResponseDto updatedOrder = orderService.updateOrderStatus(orderId, statusRequest.getStatus());
        return ResponseEntity.ok(updatedOrder);
    }
}
