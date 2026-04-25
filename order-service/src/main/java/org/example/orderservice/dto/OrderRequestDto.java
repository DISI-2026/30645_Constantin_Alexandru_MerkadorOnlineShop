package org.example.orderservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrderRequestDto {
    private UUID customerId;
    private String deliveryAddress;
    private List<OrderLineDto> items;
}
