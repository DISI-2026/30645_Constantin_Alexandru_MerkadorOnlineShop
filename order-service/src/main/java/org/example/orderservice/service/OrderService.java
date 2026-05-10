package org.example.orderservice.service;

import org.example.orderservice.dto.CheckoutRequestDto;
import org.example.orderservice.dto.OrderRequestDto;
import org.example.orderservice.dto.OrderResponseDto;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderResponseDto createOrder(OrderRequestDto orderRequestDto);
    OrderResponseDto checkout(CheckoutRequestDto checkoutRequest);
    OrderResponseDto getOrderById(UUID orderId);
    List<OrderResponseDto> getAllOrders();
    List<OrderResponseDto> getAllOrdersForAdmin();
    List<OrderResponseDto> getOrdersForSeller();
    OrderResponseDto updateOrderStatus(UUID orderId, String newStatus);
    OrderResponseDto cancelOrder(UUID orderId);
}
