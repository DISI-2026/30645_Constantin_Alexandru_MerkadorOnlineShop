package org.example.postservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ReviewApprovedMessage(UUID productId, BigDecimal newAvgRating, int reviewCount) {}
