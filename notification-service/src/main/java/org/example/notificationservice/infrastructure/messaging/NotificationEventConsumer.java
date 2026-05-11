package org.example.notificationservice.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.config.RabbitMQConfig;
import org.example.notificationservice.dto.event.OrderPlacedEvent;
import org.example.notificationservice.dto.event.OrderStatusChangedEvent;
import org.example.notificationservice.dto.event.ReviewNotificationEvent;
import org.example.notificationservice.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE_FOR_NOTIFICATION)
    @Service
    public static class OrderEventHandlers {
        
        private final NotificationService notificationService;

        public OrderEventHandlers(NotificationService notificationService) {
            this.notificationService = notificationService;
        }

        @RabbitHandler
        public void handleOrderPlaced(OrderPlacedEvent event) {
            log.info("Received OrderPlacedEvent for orderId: {}", event.getOrderId());
            try {
                notificationService.processOrderPlaced(event);
            } catch (Exception e) {
                log.error("Failed to process OrderPlacedEvent: {}", e.getMessage(), e);
            }
        }

        @RabbitHandler
        public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
            log.info("Received OrderStatusChangedEvent for orderId: {}", event.getOrderId());
            try {
                notificationService.processOrderStatusChanged(event);
            } catch (Exception e) {
                log.error("Failed to process OrderStatusChangedEvent: {}", e.getMessage(), e);
            }
        }
        
        @RabbitHandler(isDefault = true)
        public void handleUnknown(Object object) {
            log.warn("Received unknown message type on order queue: {}", object.getClass().getName());
        }
    }

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
