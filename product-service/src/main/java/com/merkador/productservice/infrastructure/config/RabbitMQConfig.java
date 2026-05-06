package com.merkador.productservice.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchanges.product}")
    private String productExchange;

    @Value("${app.rabbitmq.queues.es-sync}")
    private String esSyncQueue;

    @Value("${app.rabbitmq.queues.order-reserve}")
    private String orderReserveQueue;

    @Value("${app.rabbitmq.queues.order-release}")
    private String orderReleaseQueue;

    @Value("${app.rabbitmq.routing-keys.es-sync}")
    private String esSyncRoutingKey;

    @Value("${app.rabbitmq.routing-keys.order-reserve}")
    private String orderReserveRoutingKey;

    @Value("${app.rabbitmq.routing-keys.order-release}")
    private String orderReleaseRoutingKey;

    @Bean
    public TopicExchange productExchange() {
        return new TopicExchange(productExchange, true, false);
    }

    @Bean
    public Queue esSyncQueue() {
        return QueueBuilder.durable(esSyncQueue).build();
    }

    @Bean
    public Queue orderReserveQueue() {
        return QueueBuilder.durable(orderReserveQueue).build();
    }

    @Bean
    public Queue orderReleaseQueue() {
        return QueueBuilder.durable(orderReleaseQueue).build();
    }

    @Bean
    public Binding esSyncBinding() {
        return BindingBuilder.bind(esSyncQueue()).to(productExchange()).with(esSyncRoutingKey);
    }

    @Bean
    public Binding orderReserveBinding() {
        return BindingBuilder.bind(orderReserveQueue()).to(productExchange()).with(orderReserveRoutingKey);
    }

    @Bean
    public Binding orderReleaseBinding() {
        return BindingBuilder.bind(orderReleaseQueue()).to(productExchange()).with(orderReleaseRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}


