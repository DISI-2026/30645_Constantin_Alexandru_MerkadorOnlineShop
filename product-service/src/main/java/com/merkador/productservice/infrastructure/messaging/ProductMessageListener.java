package com.merkador.productservice.infrastructure.messaging;

import com.merkador.productservice.core.port.in.ProductUseCase;
import com.merkador.productservice.infrastructure.messaging.event.OrderStockReserveMessage;
import com.merkador.productservice.infrastructure.messaging.event.ReviewApprovedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductMessageListener {

    private final ProductUseCase productUseCase;

    /**
     * Listens for stock-reserve requests from Order Service.
     */
    @RabbitListener(queues = "${app.rabbitmq.queues.order-reserve}")
    public void handleStockReserve(OrderStockReserveMessage message) {
        log.info("Received stock reserve request: productId={} qty={} orderId={}",
                message.productId(), message.quantity(), message.orderId());
        try {
            productUseCase.reserveStock(message.productId(), message.quantity());
        } catch (Exception e) {
            log.error("Failed to reserve stock for product {}: {}", message.productId(), e.getMessage());
            // In production: send NACK or publish compensation event to Order Service
        }
    }

    /**
     * Listens for stock-release requests from Order Service (on cancellation).
     */
    @RabbitListener(queues = "${app.rabbitmq.queues.order-release}")
    public void handleStockRelease(OrderStockReserveMessage message) {
        log.info("Received stock release request: productId={} qty={} orderId={}",
                message.productId(), message.quantity(), message.orderId());
        try {
            productUseCase.releaseStock(message.productId(), message.quantity());
        } catch (Exception e) {
            log.error("Failed to release stock for product {}: {}", message.productId(), e.getMessage());
        }
    }

    /**
     * Listens for review-approved events from Post/Rating/Review Service.
     * Updates denormalized avg_rating and review_count on the product.
     */
    @RabbitListener(queues = "${app.rabbitmq.queues.es-sync}")
    public void handleReviewApproved(ReviewApprovedMessage message) {
        log.info("Received review approved: productId={} newAvg={} count={}",
                message.productId(), message.newAvgRating(), message.reviewCount());
        try {
            productUseCase.updateRating(message.productId(), message.newAvgRating(), message.reviewCount());
        } catch (Exception e) {
            log.error("Failed to update rating for product {}: {}", message.productId(), e.getMessage());
        }
    }
}
