package org.example.orderservice.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_EXCHANGE = "order.events.exchange";

    public static final String ORDER_QUEUE_FOR_PRODUCT = "product.service.order.queue";
    public static final String ORDER_QUEUE_FOR_NOTIFICATION = "notification.service.order.queue";

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

    // --- PRODUCER ---

    @Bean
    public FanoutExchange orderExchange() {
        return new FanoutExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Queue orderQueueForProduct() {
        return new Queue(ORDER_QUEUE_FOR_PRODUCT);
    }

    @Bean
    public Queue orderQueueForNotification() {
        return new Queue(ORDER_QUEUE_FOR_NOTIFICATION);
    }

    @Bean
    public Binding bindingProduct(FanoutExchange orderExchange, Queue orderQueueForProduct) {
        return BindingBuilder.bind(orderQueueForProduct).to(orderExchange);
    }

    @Bean
    public Binding bindingNotification(FanoutExchange orderExchange, Queue orderQueueForNotification) {
        return BindingBuilder.bind(orderQueueForNotification).to(orderExchange);
    }

    // --- CONSUMER ---

    @Bean
    public Queue userClientQueue() {
        return new Queue(USER_CLIENT_QUEUE);
    }
}
