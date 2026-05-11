package org.example.orderservice.infrastructure.messaging;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

    // Exchange for direct communication with product-service
    public static final String PRODUCT_SERVICE_EXCHANGE = "product.exchange";
    public static final String ORDER_RESERVE_ROUTING_KEY = "product.order.reserve";
    public static final String ORDER_RELEASE_ROUTING_KEY = "product.order.release";

    public static final String USER_MS_ORDER_EXCHANGE = "order-exchange";
    public static final String SELLER_SALES_ROUTING_KEY = "order.seller.sales";

    public static final String AUTH_EXCHANGE = "auth-exchange";
    public static final String ORDER_SERVICE_USER_SYNC_QUEUE = "order.service.user.sync.queue";
    public static final String USER_DELETED_ROUTING_KEY = "auth.user.deleted";

    public static final String USER_CLIENT_QUEUE = "user.client.events.queue";

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    // --- PRODUCER CONFIGURATIONS ---

    @Bean
    public FanoutExchange orderEventsExchange() {
        return new FanoutExchange(ORDER_EVENTS_EXCHANGE);
    }

    @Bean
    public TopicExchange productServiceExchange() {
        return new TopicExchange(PRODUCT_SERVICE_EXCHANGE);
    }

    @Bean
    public TopicExchange userMsOrderExchange() {
        return new TopicExchange(USER_MS_ORDER_EXCHANGE);
    }

    // --- CONSUMER CONFIGURATIONS ---

    @Bean
    public TopicExchange authExchange() {
        return new TopicExchange(AUTH_EXCHANGE);
    }

    @Bean
    public Queue orderUserSyncQueue() {
        return new Queue(ORDER_SERVICE_USER_SYNC_QUEUE, true);
    }

    @Bean
    public Binding bindOrderUserSyncQueue() {
        return BindingBuilder.bind(orderUserSyncQueue()).to(authExchange()).with(USER_DELETED_ROUTING_KEY);
    }

    @Bean
    public Queue userClientQueue() {
        return new Queue(USER_CLIENT_QUEUE);
    }
}
