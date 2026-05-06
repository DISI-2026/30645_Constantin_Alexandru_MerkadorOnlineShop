package com.merkador.productservice.infrastructure.messaging.event;

import java.util.UUID;

public record ProductUpdatedEvent(UUID productId, UUID sellerId) {}


