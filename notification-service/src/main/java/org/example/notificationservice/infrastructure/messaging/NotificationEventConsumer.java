package org.example.notificationservice.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.config.RabbitMQConfig;
import org.example.notificationservice.dto.event.ReviewNotificationEvent;
import org.example.notificationservice.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationService notificationService;

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
