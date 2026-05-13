package com.merkador.productservice.infrastructure.messaging.event;

import java.util.UUID;

public record SellerRatingMessage(UUID vendorId, Double averageRating) {}
