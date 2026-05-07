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
import org.springframework.amqp.core.Message;
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
     * The __TypeId__ header from order-service identifies the concrete type.
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE_FOR_NOTIFICATION)
    public void handleOrderEvent(Message message) {
        String typeId = message.getMessageProperties().getHeader("__TypeId__");
        String body = new String(message.getBody(), StandardCharsets.UTF_8);

        try {
            if (typeId != null && typeId.contains("OrderPlacedEvent")) {
                OrderPlacedEvent event = objectMapper.readValue(body, OrderPlacedEvent.class);
                notificationService.processOrderPlaced(event);
            } else if (typeId != null && typeId.contains("OrderStatusChangedEvent")) {
                OrderStatusChangedEvent event = objectMapper.readValue(body, OrderStatusChangedEvent.class);
                notificationService.processOrderStatusChanged(event);
            } else {
                log.warn("Received unknown order event type: {}", typeId);
            }
        } catch (Exception e) {
            log.error("Failed to process order event (type={}): {}", typeId, e.getMessage(), e);
        }
    }

    /**
     * Handles review notification events from the post-service.
     * TypePrecedence.INFERRED in the converter lets Spring deserialize to ReviewNotificationEvent
     * based on the method signature, ignoring the producer's __TypeId__ header.
     */
    @RabbitListener(queues = RabbitMQConfig.REVIEW_QUEUE_FOR_NOTIFICATION)
    public void handleReviewEvent(ReviewNotificationEvent event) {
        try {
            notificationService.processReviewPosted(event);
        } catch (Exception e) {
            log.error("Failed to process review event: {}", e.getMessage(), e);
        }
    }
}
