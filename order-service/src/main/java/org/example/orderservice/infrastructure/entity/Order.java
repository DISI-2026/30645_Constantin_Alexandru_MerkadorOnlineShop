package org.example.orderservice.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ORDERS")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "customerId", nullable = false)
    private UUID customerId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "totalAmount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "deliveryAddress")
    private String deliveryAddress;

    @Column(name = "placedAt", nullable = false)
    private LocalDateTime placedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> orderLines;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderStatusHistory> statusHistory;
}
