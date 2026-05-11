package org.example.orderservice.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.orderservice.dto.event.UserSyncMessage;
import org.example.orderservice.infrastructure.entity.Order;
import org.example.orderservice.infrastructure.repository.OrderRepository;
import org.example.orderservice.service.CartService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSyncConsumer {

    private final OrderRepository orderRepository;
    private final CartService cartService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_SERVICE_USER_SYNC_QUEUE)
    @Transactional
    public void handleUserDeleted(@Payload UserSyncMessage message) {
        log.info("Received user deleted event for userId: {}", message.getUserId());
        try {
            List<Order> userOrders = orderRepository.findByCustomerId(message.getUserId());
            if (!userOrders.isEmpty()) {
                orderRepository.deleteAll(userOrders);
                log.info("Deleted {} orders for customerId: {}", userOrders.size(), message.getUserId());
            }

            cartService.clearCartForUser(message.getUserId());
            
        } catch (Exception e) {
            log.error("Error processing user deleted event: {}", e.getMessage(), e);
        }
    }
}
