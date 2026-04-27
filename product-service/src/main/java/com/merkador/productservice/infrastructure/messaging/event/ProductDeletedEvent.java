package com.merkador.productservice.infrastructure.messaging.event;

import java.util.UUID;

public record ProductDeletedEvent(UUID productId, UUID vendorId) {}
