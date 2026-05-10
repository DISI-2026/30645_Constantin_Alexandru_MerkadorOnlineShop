package org.example.orderservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStockReserveMessage {
    private UUID productId;
    private int quantity;
    private UUID orderId;
}
