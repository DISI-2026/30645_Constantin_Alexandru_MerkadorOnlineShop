package org.example.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "auth-exchange";
    public static final String QUEUE_NAME = "user-profile-queue";

    // Pattern-ul folosit pentru a lega coada de exchange (prinde tot ce începe cu auth.user.)
    public static final String ROUTING_KEY_PATTERN = "auth.user.#";

    // Cheile exacte pe care le folosește Producătorul când trimite mesajul
    // Nota: nu avem nevoie de update, pentru ca update-ul se face de la User MS, iar aici se fac doar resetari de parola, email sau promovari de rol/status, deci nu are legatura cu user profile
    public static final String ROUTING_KEY_CREATED = "auth.user.created";
    public static final String ROUTING_KEY_DELETED = "auth.user.deleted";

    @Bean
    public TopicExchange authExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue userProfileQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding binding(Queue userProfileQueue, TopicExchange authExchange) {
        return BindingBuilder.bind(userProfileQueue).to(authExchange).with(ROUTING_KEY_PATTERN);
    }

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
}