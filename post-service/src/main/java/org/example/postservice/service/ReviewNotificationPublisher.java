package org.example.postservice.service;

import org.example.postservice.dto.ReviewApprovedMessage;
import org.example.postservice.dto.ReviewResponseDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ReviewNotificationPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;
    private final String routingKey;

    private final String productExchange = "product.exchange";
    private final String productRoutingKey = "product.es.sync";

    public ReviewNotificationPublisher(RabbitTemplate rabbitTemplate,
                                       @Value("${rabbitmq.exchange.name:review_exchange}") String exchangeName,
                                       @Value("${rabbitmq.routing.key:review.notifications}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
    }

    public void publishReviewNotification(ReviewResponseDto review) {
        rabbitTemplate.convertAndSend(exchangeName, routingKey, review);
    }
    
    public void publishRatingUpdateToProductService(ReviewApprovedMessage message) {
        rabbitTemplate.convertAndSend(productExchange, productRoutingKey, message);
    }
}
