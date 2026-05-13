package com.merkador.productservice.core.port.out;

import java.util.Set;
import java.util.UUID;

public interface SellerVerificationPort {
    boolean isSellerVerifiedForCategory(UUID sellerId, String categorySlug);
}
