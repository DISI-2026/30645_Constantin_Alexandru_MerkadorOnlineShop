package org.example.services;

import org.example.entities.UserProfile;
import org.example.repositories.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.example.ports.AvatarStoragePort;
import java.util.UUID;

@Service
public class UserSyncConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserSyncConsumer.class);
    private final UserProfileRepository userProfileRepository;
    private final AvatarStoragePort avatarStoragePort;

    public UserSyncConsumer(UserProfileRepository userProfileRepository, AvatarStoragePort avatarStoragePort) {
        this.userProfileRepository = userProfileRepository;
        this.avatarStoragePort = avatarStoragePort;
    }

    @RabbitListener(queues = "user-profile-queue")
    @Transactional
    public void receiveMessage(@Payload UserSyncMessage message,
                               @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {

        LOGGER.info("Received message with routing key: {}", routingKey);

        try {
            if ("auth.user.created".equals(routingKey)) {
                handleUserCreated(message);
            } else if ("auth.user.deleted".equals(routingKey)) {
                handleUserDeleted(message.getUserId());
            }
        } catch (Exception e) {
            LOGGER.error("Error processing sync message: {}", e.getMessage());
        }
    }

    private void handleUserCreated(UserSyncMessage message) {
        LOGGER.info("Creating profile for user ID: {}", message.getUserId());

        // Verificăm dacă nu există deja
        if (userProfileRepository.existsById(message.getUserId())) {
            LOGGER.warn("User profile already exists for ID: {}", message.getUserId());
            return;
        }

        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(message.getUserId());
        userProfile.setFirstName(message.getFirstName());
        userProfile.setLastName(message.getLastName());

        userProfileRepository.save(userProfile);
        LOGGER.info("Profile created successfully for: {} {}", message.getFirstName(), message.getLastName());
    }

    private void handleUserDeleted(UUID userId) {
        LOGGER.info("Deleting profile for user ID: {}", userId);
        userProfileRepository.deleteById(userId);
        avatarStoragePort.deleteAvatarByUrl(userId.toString());
    }

    /**
     * DTO for the incoming message from RabbitMQ
     * Holds only the fields sent by the producer of the message
     */
    public static class UserSyncMessage {
        private UUID userId;
        private String firstName;
        private String lastName;

        public UserSyncMessage() {}

        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }
}