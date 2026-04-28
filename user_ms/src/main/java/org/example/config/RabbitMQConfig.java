package org.example.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "auth-exchange";
    public static final String QUEUE_NAME = "user-profile-queue";

    // Pattern-ul folosit pentru a lega coada de exchange
    public static final String ROUTING_KEY_PATTERN = "auth.user.#";

    @Bean
    public TopicExchange authExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue userProfileQueue() {
        return new Queue(QUEUE_NAME, true); // true = durable (coada supraviețuiește restarturilor)
    }

    @Bean
    public Binding binding(Queue userProfileQueue, TopicExchange authExchange) {
        return BindingBuilder.bind(userProfileQueue).to(authExchange).with(ROUTING_KEY_PATTERN);
    }

    // Foarte important pentru Consumator: transformă automat JSON-ul primit în obiect Java
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}