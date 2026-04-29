package org.example.repositories;

import org.example.entities.SellerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SellerProfileRepository extends JpaRepository<SellerProfile, UUID> {
    Optional<SellerProfile> findByShopSlug(String shopSlug);
    List<SellerProfile> findByVerifiedFalse();
}