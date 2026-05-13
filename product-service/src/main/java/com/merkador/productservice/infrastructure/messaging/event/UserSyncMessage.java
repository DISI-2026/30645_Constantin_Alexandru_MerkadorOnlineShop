package com.merkador.productservice.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSyncMessage {
    private UUID userId;
    private String firstName;
    private String lastName;
}
