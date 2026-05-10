package org.example.orderservice.mapper;

import org.example.orderservice.dto.OrderLineDto;
import org.example.orderservice.dto.OrderResponseDto;
import org.example.orderservice.dto.OrderStatusHistoryDto;
import org.example.orderservice.infrastructure.entity.Order;
import org.example.orderservice.infrastructure.entity.OrderLine;
import org.example.orderservice.infrastructure.entity.OrderStatusHistory;

import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderResponseDto toDto(Order order) {
        return OrderResponseDto.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .deliveryAddress(order.getDeliveryAddress())
                .placedAt(order.getPlacedAt())
                .items(order.getOrderLines().stream()
                        .map(OrderMapper::toDto)
                        .collect(Collectors.toList()))
                .statusHistory(order.getStatusHistory().stream()
                        .map(OrderMapper::toDto)
                        .collect(Collectors.toList()))
                .build();
    }

    public static OrderLineDto toDto(OrderLine orderLine) {
        return OrderLineDto.builder()
                .productId(orderLine.getProductId())
                .productTitle(orderLine.getProductTitle())
                .unitPrice(orderLine.getUnitPrice())
                .quantity(orderLine.getQuantity())
                .sellerId(orderLine.getSellerId())
                .build();
    }

    public static OrderStatusHistoryDto toDto(OrderStatusHistory statusHistory) {
        return OrderStatusHistoryDto.builder()
                .fromStatus(statusHistory.getFromStatus())
                .toStatus(statusHistory.getToStatus())
                .changedAt(statusHistory.getChangedAt())
                .build();
    }
}
