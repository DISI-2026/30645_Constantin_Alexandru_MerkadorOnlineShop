package org.example.notificationservice.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewNotificationEvent {
    private UUID id;
    private UUID customerId;
    private UUID productId;
    private UUID orderId;
    private Integer rating;
    private String body;
    private String status;
    private LocalDateTime createdAt;
}
