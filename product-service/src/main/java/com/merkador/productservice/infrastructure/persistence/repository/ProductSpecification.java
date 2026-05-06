package com.merkador.productservice.infrastructure.persistence.repository;

import com.merkador.productservice.core.port.in.ProductFilter;
import com.merkador.productservice.infrastructure.persistence.entity.ProductEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    private ProductSpecification() {}

    public static Specification<ProductEntity> of(ProductFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), filter.getCategoryId()));
            }
            if (filter.getSellerId() != null) {
                predicates.add(cb.equal(root.get("sellerId"), filter.getSellerId()));
            }
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }
            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }
            if (filter.getMinRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("avgRating"), filter.getMinRating()));
            }
            if (filter.getSearchTerm() != null && !filter.getSearchTerm().isBlank()) {
                String pattern = "%" + filter.getSearchTerm().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}


