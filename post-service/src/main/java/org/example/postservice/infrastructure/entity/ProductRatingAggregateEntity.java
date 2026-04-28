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
    private String productId;

    @Column(precision = 3, scale = 2)
    private BigDecimal avgRating;

    private long reviewCount;
    private long count1Star;
    private long count2Star;
    private long count3Star;
    private long count4Star;
    private long count5Star;
}
