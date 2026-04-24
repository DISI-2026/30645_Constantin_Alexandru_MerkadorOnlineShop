package org.example.services;

import org.example.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class UserSyncConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserSyncConsumer.class);
    private final CredentialService credentialService;

    public UserSyncConsumer(CredentialService credentialService) {
        this.credentialService = credentialService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveMessage(UserSyncMessage message, @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String key) {
        LOGGER.info("Received sync event for User ID: {}", message.getUserId());

        try {
            if (key.endsWith("created")) {
                credentialService.register(message.getUserId(), message.getUserJsonDetails());
            } else if (key.endsWith("updated")) {
                credentialService.update(message.getUserId(), message.getUserJsonDetails());
            } else if (key.endsWith("deleted")) {
                credentialService.delete(message.getUserId());
            }

        } catch (Exception e) {
            LOGGER.error("Failed to process message for user {}", message.getUserId(), e);
        }
    }

    // DTO Class
    public static class UserSyncMessage {
        private UUID userId;
        private String userJsonDetails;

        public UserSyncMessage() {} // Empty constructor needed for Jackson

        public UserSyncMessage(UUID userId, String userJsonDetails) {
            this.userId = userId;
            this.userJsonDetails = userJsonDetails;
        }

        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }

        public String getUserJsonDetails() { return userJsonDetails; }
        public void setUserJsonDetails(String userJsonDetails) { this.userJsonDetails = userJsonDetails; }
    }
}