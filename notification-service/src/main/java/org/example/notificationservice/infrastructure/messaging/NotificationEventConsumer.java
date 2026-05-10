package org.example.notificationservice.infrastructure.messaging;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.config.RabbitMQConfig;
import org.example.notificationservice.dto.event.OrderPlacedEvent;
import org.example.notificationservice.dto.event.OrderStatusChangedEvent;
import org.example.notificationservice.dto.event.ReviewNotificationEvent;
import org.example.notificationservice.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Handles both OrderPlacedEvent and OrderStatusChangedEvent from the fanout exchange.
     * Receives raw message bytes to avoid Spring's Jackson2MessageConverter type mapping.
     * Detects event type based on JSON content.
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE_FOR_NOTIFICATION)
    public void handleOrderEvent(byte[] messageBody) {
        String body = new String(messageBody, StandardCharsets.UTF_8);

        log.info("Received order event. Body: {}", body);

        try {
            // Try to determine type: OrderStatusChangedEvent has oldStatus, OrderPlacedEvent has items array
            if (body.contains("\"oldStatus\"") || body.contains("oldStatus")) {
                OrderStatusChangedEvent event = objectMapper.readValue(body, OrderStatusChangedEvent.class);
                notificationService.processOrderStatusChanged(event);
                log.info("Processed OrderStatusChangedEvent for orderId: {}", event.getOrderId());
            } else {
                OrderPlacedEvent event = objectMapper.readValue(body, OrderPlacedEvent.class);
                notificationService.processOrderPlaced(event);
                log.info("Processed OrderPlacedEvent for orderId: {}", event.getOrderId());
            }
        } catch (Exception e) {
            log.error("Failed to process order event: {}", e.getMessage(), e);
        }
    }

    /**
     * Handles review notification events from the post-service.
     */
    @RabbitListener(queues = RabbitMQConfig.REVIEW_QUEUE_FOR_NOTIFICATION)
    public void handleReviewEvent(ReviewNotificationEvent event) {
        log.info("Received review event for productId: {}", event.getProductId());
        try {
            notificationService.processReviewPosted(event);
        } catch (Exception e) {
            log.error("Failed to process review event: {}", e.getMessage(), e);
        }
    }
}
