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

    // --- AUTH ---
    public static final String EXCHANGE_NAME = "auth-exchange";
    public static final String QUEUE_NAME = "user-profile-queue";
    public static final String ROUTING_KEY_PATTERN = "auth.user.#";

    // --- ORDER & PRODUCT ---
    public static final String ORDER_EXCHANGE = "order-exchange";
    public static final String PRODUCT_EXCHANGE = "product-exchange";

    public static final String SELLER_SALES_QUEUE = "seller-sales-queue";
    public static final String SELLER_RATING_QUEUE = "seller-rating-queue";

    public static final String ORDER_SALES_ROUTING_KEY = "order.seller.sales";
    public static final String PRODUCT_RATING_ROUTING_KEY = "product.seller.rating";

    // ==========================================
    // EXCHANGES
    // ==========================================
    @Bean
    public TopicExchange authExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public TopicExchange productExchange() {
        return new TopicExchange(PRODUCT_EXCHANGE);
    }

    // ==========================================
    // QUEUES
    // ==========================================
    @Bean
    public Queue userProfileQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Queue sellerSalesQueue() {
        return new Queue(SELLER_SALES_QUEUE, true);
    }

    @Bean
    public Queue sellerRatingQueue() {
        return new Queue(SELLER_RATING_QUEUE, true);
    }

    // ==========================================
    // BINDINGS
    // ==========================================
    @Bean
    public Binding bindingUser(Queue userProfileQueue, TopicExchange authExchange) {
        return BindingBuilder.bind(userProfileQueue).to(authExchange).with(ROUTING_KEY_PATTERN);
    }

    @Bean
    public Binding bindingSellerSales(Queue sellerSalesQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(sellerSalesQueue).to(orderExchange).with(ORDER_SALES_ROUTING_KEY);
    }

    @Bean
    public Binding bindingSellerRating(Queue sellerRatingQueue, TopicExchange productExchange) {
        return BindingBuilder.bind(sellerRatingQueue).to(productExchange).with(PRODUCT_RATING_ROUTING_KEY);
    }

    // ==========================================
    // CONVERTER
    // ==========================================
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}