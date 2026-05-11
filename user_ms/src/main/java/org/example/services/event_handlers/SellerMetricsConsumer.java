package org.example.services.event_handlers;

import org.example.entities.SellerProfile;
import org.example.repositories.SellerProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class SellerMetricsConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SellerMetricsConsumer.class);
    private final SellerProfileRepository sellerProfileRepository;

    public SellerMetricsConsumer(SellerProfileRepository sellerProfileRepository) {
        this.sellerProfileRepository = sellerProfileRepository;
    }

    /**
     * Listens to messages coming from ORDER-MS (adds to the total revenue from sales when a new sale has completed)
     */
    @RabbitListener(queues = "seller-sales-queue")
    @Transactional
    public void receiveSalesMessage(@Payload SellerSalesMessage message) {
        try {
            Optional<SellerProfile> profileOpt = sellerProfileRepository.findById(message.getVendorId());

            if (profileOpt.isPresent()) {
                SellerProfile seller = profileOpt.get();

                Double currentSales = seller.getTotalSales();
                Double amountToAdd = message.getTotalAmount();

                seller.setTotalSales(currentSales + amountToAdd);

                sellerProfileRepository.save(seller);
                LOGGER.info("Updated total sales for seller {}", message.getVendorId());
            } else {
                LOGGER.warn("Received sales update for unknown seller ID: {}", message.getVendorId());
            }
        } catch (Exception e) {
            LOGGER.error("Error processing sales message: {}", e.getMessage());
        }
    }

    /**
     * Listens to messages coming from PRODUCT-MS (updates average rating for a seller, the product-ms calculates the average rating itself, we just store it here)
     */
    @RabbitListener(queues = "seller-rating-queue")
    @Transactional
    public void receiveRatingMessage(@Payload SellerRatingMessage message) {
        try {
            Optional<SellerProfile> profileOpt = sellerProfileRepository.findById(message.getVendorId());

            if (profileOpt.isPresent()) {
                SellerProfile seller = profileOpt.get();

                // just overwrite the rating, the product-ms calculates the average rating itself
                seller.setAvgRating(message.getAverageRating());

                sellerProfileRepository.save(seller);
                LOGGER.info("Updated average rating for seller {}", message.getVendorId());
            } else {
                LOGGER.warn("Received rating update for unknown seller ID: {}", message.getVendorId());
            }
        } catch (Exception e) {
            LOGGER.error("Error processing seller rating message: {}", e.getMessage());
        }
    }

    // ========================================================
    // DTOs for the incoming messages from RabbitMQ
    // ========================================================

    public static class SellerSalesMessage {
        private UUID vendorId;
        private Double totalAmount;

        public SellerSalesMessage() {}

        public UUID getVendorId() { return vendorId; }
        public void setVendorId(UUID vendorId) { this.vendorId = vendorId; }

        public Double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    }

    public static class SellerRatingMessage {
        private UUID vendorId;
        private Double averageRating;

        public SellerRatingMessage() {}

        public UUID getVendorId() { return vendorId; }
        public void setVendorId(UUID vendorId) { this.vendorId = vendorId; }

        public Double getAverageRating() { return averageRating; }
        public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    }
}