package org.example.postservice.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "product_rating_aggregates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRatingAggregateEntity {

    @Id
    @Column(name = "product_id")
    private String productId;

    @Column(name = "avg_rating", precision = 3, scale = 2, nullable = false)
    private BigDecimal avgRating;

    @Column(name = "review_count", nullable = false)
    private long reviewCount;

    @Column(name = "count1_star", nullable = false)
    private long count1Star;

    @Column(name = "count2_star", nullable = false)
    private long count2Star;

    @Column(name = "count3_star", nullable = false)
    private long count3Star;

    @Column(name = "count4_star", nullable = false)
    private long count4Star;

    @Column(name = "count5_star", nullable = false)
    private long count5Star;
}
