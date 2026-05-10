package org.example.orderservice.infrastructure.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange for broadcasting order events (to notifications, etc.)
    public static final String ORDER_EVENTS_EXCHANGE = "order.events.exchange";
    public static final String ORDER_QUEUE_FOR_NOTIFICATION = "notification.service.order.queue";

    // Exchange for direct communication with product-service
    public static final String PRODUCT_SERVICE_EXCHANGE = "product.exchange";
    public static final String ORDER_RESERVE_ROUTING_KEY = "product.order.reserve";
    public static final String ORDER_RELEASE_ROUTING_KEY = "product.order.release";

    // Queue for consuming events from user-service
    public static final String USER_CLIENT_QUEUE = "user.client.events.queue";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    // --- PRODUCER CONFIGURATIONS ---

    // 1. Fanout exchange for broadcasting general order events
    @Bean
    public FanoutExchange orderEventsExchange() {
        return new FanoutExchange(ORDER_EVENTS_EXCHANGE);
    }

    // 2. Topic exchange for specific commands to product-service
    @Bean
    public TopicExchange productServiceExchange() {
        return new TopicExchange(PRODUCT_SERVICE_EXCHANGE);
    }

    // --- CONSUMER CONFIGURATIONS ---

    @Bean
    public Queue userClientQueue() {
        return new Queue(USER_CLIENT_QUEUE);
    }
}
