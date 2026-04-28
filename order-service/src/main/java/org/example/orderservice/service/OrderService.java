package org.example.orderservice.service;

import org.example.orderservice.dto.OrderRequestDto;
import org.example.orderservice.dto.OrderResponseDto;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderResponseDto createOrder(OrderRequestDto orderRequestDto);
    OrderResponseDto getOrderById(UUID orderId);
    List<OrderResponseDto> getAllOrders();
    OrderResponseDto updateOrderStatus(UUID orderId, String newStatus);
}
