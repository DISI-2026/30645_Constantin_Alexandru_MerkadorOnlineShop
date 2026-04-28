package org.example.orderservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrderResponseDto {
    private UUID id;
    private UUID customerId;
    private String status;
    private BigDecimal totalAmount;
    private String deliveryAddress;
    private LocalDateTime placedAt;
    private List<OrderLineDto> items;
    private List<OrderStatusHistoryDto> statusHistory;
}
