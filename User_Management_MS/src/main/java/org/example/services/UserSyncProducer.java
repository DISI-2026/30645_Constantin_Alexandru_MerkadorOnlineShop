package org.example.services;

import org.example.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserSyncProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserSyncProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public UserSyncProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // We use routing keys to distinguish events: "user.created", "user.updated", "user.deleted"
    public void sendUserCreated(UUID userId, String userJson) {
        sendMessage("user.created", userId, userJson);
    }

    public void sendUserUpdated(UUID userId, String userJson) {
        sendMessage("user.updated", userId, userJson);
    }

    public void sendUserDeleted(UUID userId) {
        sendMessage("user.deleted", userId, null);
    }

    private void sendMessage(String routingKey, UUID userId, String payload) {
        // Create a structured message object
        UserSyncMessage message = new UserSyncMessage(userId, payload);

        LOGGER.info("Sending message to Exchange: [{}] Routing Key: [{}] Payload: [{}]",
                RabbitMQConfig.EXCHANGE_NAME, routingKey, message);

        // Convert and Send to the Exchange
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, message);
    }

    // Inner DTO class for the message structure
    public static class UserSyncMessage {
        private UUID userId;
        private String userJsonDetails; // The full JSON

        public UserSyncMessage(UUID userId, String userJsonDetails) {
            this.userId = userId;
            this.userJsonDetails = userJsonDetails;
        }

        // Getters and Setters (Required for Jackson serialization)
        public UUID getUserId() { return userId; }
        public String getUserJsonDetails() { return userJsonDetails; }
    }
}