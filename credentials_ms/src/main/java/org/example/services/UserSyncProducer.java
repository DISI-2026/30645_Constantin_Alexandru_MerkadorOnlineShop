package org.example.services;

import org.example.config.RabbitMQConfig;
import org.example.dtos.RegisterReqDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserSyncProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserSyncProducer.class);
    private final RabbitTemplate rabbitTemplate;

    public UserSyncProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendUserCreatedEvent(UUID userId, RegisterReqDTO registerDTO) {
        // Construim mesajul extrăgând doar datele non-sensibile de care are nevoie User MS din fat DTO-ul registerDTO
        AuthSyncMessage message = new AuthSyncMessage(
                userId,
                registerDTO.getFirstName(),
                registerDTO.getLastName()
        );

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY_CREATED,
                    message
            );
        } catch (Exception e) {
            // Re-aruncăm excepția.
            // Asta îi va spune metodei din CredentialService să anuleze salvarea în baza de date
            // (Rollback la tranzacție) dacă RabbitMQ a picat, ca să nu avem useri orfani.
            throw new RuntimeException("Could not synchronize user creation", e);
        }
    }

    public void sendUserDeletedEvent(UUID userId){
        AuthSyncMessage message = new AuthSyncMessage(userId, null, null);
        try{
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY_DELETED,
                    message);
        } catch (Exception e){
            // Rollback din CredentialService daca s-a ajuns aici
            throw new RuntimeException("Could not synchronize user deletion", e);
        }
    }

    // --- Clasa internă DTO pentru payload-ul trimis pe coadă ---
    public static class AuthSyncMessage {
        private UUID userId;
        private String firstName;
        private String lastName;

        public AuthSyncMessage() {}

        public AuthSyncMessage(UUID userId, String firstName, String lastName) {
            this.userId = userId;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        // Getters & Setters
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }
}