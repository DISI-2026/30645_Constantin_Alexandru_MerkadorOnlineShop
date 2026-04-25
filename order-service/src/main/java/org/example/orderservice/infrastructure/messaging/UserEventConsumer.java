package org.example.orderservice.infrastructure.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserEventConsumer {

    // Ascultă coada definită în RabbitMQConfig
    @RabbitListener(queues = RabbitMQConfig.USER_CLIENT_QUEUE)
    public void handleUserClientEvent(Object eventPayload) {
        // Deocamdată, nu știm exact structura evenimentului de la User Service.
        // Vom crea un DTO specific (ex: UserAddressChangedEvent) când vom avea detaliile.
        
        log.info("Received event from User-Client-Queue: {}", eventPayload);

        // TODO: Implementează logica de procesare a evenimentului.
        // Exemplu:
        // if (eventPayload instanceof UserAddressChangedEvent) {
        //     UserAddressChangedEvent event = (UserAddressChangedEvent) eventPayload;
        //     // Logica pentru a actualiza adresa default a clientului în comenzile viitoare
        // } else if (eventPayload instanceof UserPreferencesChangedEvent) {
        //     // ...
        // }
    }
}
