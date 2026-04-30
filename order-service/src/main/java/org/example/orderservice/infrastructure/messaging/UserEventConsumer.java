package org.example.orderservice.infrastructure.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserEventConsumer {
    @RabbitListener(queues = RabbitMQConfig.USER_CLIENT_QUEUE)
    public void handleUserClientEvent(Object eventPayload) {
        log.info("Received event from User-Client-Queue: {}", eventPayload);
    }
}
