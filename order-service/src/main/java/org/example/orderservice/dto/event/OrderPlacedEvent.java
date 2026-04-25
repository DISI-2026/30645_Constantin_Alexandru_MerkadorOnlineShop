package org.example.orderservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {
    private UUID orderId;
    private UUID customerId;
    private BigDecimal totalAmount;
    private LocalDateTime placedAt;
    private List<OrderItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private String productId;
        private int quantity;
    }
}
