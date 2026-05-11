package org.example.notificationservice.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    // Queue declared by order-service, bound to its fanout exchange
    public static final String ORDER_QUEUE_FOR_NOTIFICATION = "notification.service.order.queue";
    public static final String ORDER_EVENTS_EXCHANGE = "order.events.exchange";

    // Queue declared by post-service, bound to review_exchange with routing key review.notifications
    public static final String REVIEW_QUEUE_FOR_NOTIFICATION = "review_notifications";
    public static final String REVIEW_EVENTS_EXCHANGE = "review_exchange";

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);

        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("org.example.orderservice.dto.event.OrderPlacedEvent", org.example.notificationservice.dto.event.OrderPlacedEvent.class);
        idClassMapping.put("org.example.orderservice.dto.event.OrderStatusChangedEvent", org.example.notificationservice.dto.event.OrderStatusChangedEvent.class);
        typeMapper.setIdClassMapping(idClassMapping);

        typeMapper.addTrustedPackages("*");

        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    // Declare queues so the service can start even if producer has not yet created them
    @Bean
    public Queue orderQueueForNotification() {
        return new Queue(ORDER_QUEUE_FOR_NOTIFICATION, true);
    }

    @Bean
    public Queue reviewQueueForNotification() {
        return new Queue(REVIEW_QUEUE_FOR_NOTIFICATION, true);
    }

    // Declare exchanges
    @Bean
    public FanoutExchange orderEventsExchange() {
        return new FanoutExchange(ORDER_EVENTS_EXCHANGE);
    }

    @Bean
    public TopicExchange reviewEventsExchange() {
        return new TopicExchange(REVIEW_EVENTS_EXCHANGE);
    }

    // Bind queues to exchanges
    @Bean
    public Binding bindOrderQueueToOrderExchange(Queue orderQueueForNotification, FanoutExchange orderEventsExchange) {
        return BindingBuilder.bind(orderQueueForNotification).to(orderEventsExchange);
    }

    @Bean
    public Binding bindReviewQueueToReviewExchange(Queue reviewQueueForNotification, TopicExchange reviewEventsExchange) {
        return BindingBuilder.bind(reviewQueueForNotification).to(reviewEventsExchange).with("review.notifications");
    }
}
