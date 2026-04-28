package org.example.orderservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderStatusHistoryDto {
    private String fromStatus;
    private String toStatus;
    private LocalDateTime changedAt;
}
