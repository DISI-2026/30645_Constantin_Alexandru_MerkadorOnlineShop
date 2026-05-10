package org.example.orderservice.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.dto.event.OrderPlacedEvent;
import org.example.orderservice.dto.event.OrderStatusChangedEvent;
import org.example.orderservice.dto.event.OrderStockReserveMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("Publishing order placed event for orderId: {}", event.getOrderId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EVENTS_EXCHANGE, "", event);
    }

    public void publishOrderStatusChangedEvent(OrderStatusChangedEvent event) {
        log.info("Publishing order status changed event for orderId: {}. New status: {}", event.getOrderId(), event.getNewStatus());
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EVENTS_EXCHANGE, "", event);
    }

    // --- Commands to Product Service ---

    public void sendStockReserveCommand(OrderStockReserveMessage message) {
        log.info("Sending stock reserve command to Product Service for productId={} orderId={}", message.getProductId(), message.getOrderId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.PRODUCT_SERVICE_EXCHANGE, RabbitMQConfig.ORDER_RESERVE_ROUTING_KEY, message);
    }

    public void sendStockReleaseCommand(OrderStockReserveMessage message) {
        log.info("Sending stock release command to Product Service for productId={} orderId={}", message.getProductId(), message.getOrderId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.PRODUCT_SERVICE_EXCHANGE, RabbitMQConfig.ORDER_RELEASE_ROUTING_KEY, message);
    }
}
