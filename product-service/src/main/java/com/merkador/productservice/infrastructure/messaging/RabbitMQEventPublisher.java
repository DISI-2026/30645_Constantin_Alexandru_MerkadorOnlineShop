package com.merkador.productservice.infrastructure.messaging;

import com.merkador.productservice.core.port.out.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQEventPublisher implements EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchanges.product}")
    private String exchange;

    @Override
    public void publishProductCreated(Object event) {
        publish("product.created", event);
    }

    @Override
    public void publishProductUpdated(Object event) {
        publish("product.updated", event);
    }

    @Override
    public void publishProductDeleted(Object event) {
        publish("product.deleted", event);
    }

    @Override
    public void publishStockUpdated(Object event) {
        publish("product.stock.updated", event);
    }

    private void publish(String routingKey, Object event) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.debug("Published event to {}: {}", routingKey, event);
        } catch (Exception e) {
            log.error("Failed to publish event to {}: {}", routingKey, e.getMessage(), e);
        }
    }
}
