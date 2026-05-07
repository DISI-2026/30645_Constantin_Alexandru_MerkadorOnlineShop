package org.example.notificationservice.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queue declared by order-service, bound to its fanout exchange
    public static final String ORDER_QUEUE_FOR_NOTIFICATION = "notification.service.order.queue";

    // Queue declared by post-service, bound to review_exchange with routing key review.notifications
    public static final String REVIEW_QUEUE_FOR_NOTIFICATION = "review_notifications";

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        // Use method signature type for deserialization, not the __TypeId__ header from producers
        converter.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
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
}
