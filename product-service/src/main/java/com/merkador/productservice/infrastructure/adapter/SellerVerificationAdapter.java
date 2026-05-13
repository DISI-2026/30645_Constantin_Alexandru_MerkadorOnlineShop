package com.merkador.productservice.infrastructure.adapter;

import com.merkador.productservice.core.port.out.SellerVerificationPort;
import com.merkador.productservice.infrastructure.adapter.SellerProfileDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SellerVerificationAdapter implements SellerVerificationPort {

    private final RestTemplate restTemplate;

    @Override
    public boolean isSellerVerifiedForCategory(UUID sellerId, String categorySlug) {
        String url = "http://user-ms:8082/users/" + sellerId + "/seller-profile";
        try {
            SellerProfileDTO profile = restTemplate.getForObject(url, SellerProfileDTO.class);
            return profile != null && profile.isVerified() && profile.getAuthorizedCategories() != null && profile.getAuthorizedCategories().contains(categorySlug);
        } catch (Exception e) {
            return false;
        }
    }
}
