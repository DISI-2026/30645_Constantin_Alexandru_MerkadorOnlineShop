package com.merkador.productservice.infrastructure.messaging;

import com.merkador.productservice.core.port.in.ProductUseCase;
import com.merkador.productservice.infrastructure.config.RabbitMQConfig;
import com.merkador.productservice.infrastructure.messaging.event.UserSyncMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSyncConsumer {

    private final ProductUseCase productUseCase;

    @RabbitListener(queues = RabbitMQConfig.PRODUCT_USER_SYNC_QUEUE)
    public void handleUserDeleted(@Payload UserSyncMessage message) {
        log.info("Received user deleted event for userId: {}", message.getUserId());
        try {
            productUseCase.deleteAllProductsBySellerId(message.getUserId());
        } catch (Exception e) {
            log.error("Error processing user deleted event: {}", e.getMessage(), e);
        }
    }
}
