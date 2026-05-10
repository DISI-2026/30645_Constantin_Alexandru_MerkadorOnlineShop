package org.example.orderservice.service;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.dto.*;
import org.example.orderservice.dto.event.OrderPlacedEvent;
import org.example.orderservice.dto.event.OrderStatusChangedEvent;
import org.example.orderservice.infrastructure.entity.Order;
import org.example.orderservice.infrastructure.entity.OrderLine;
import org.example.orderservice.infrastructure.entity.OrderStatusHistory;
import org.example.orderservice.infrastructure.messaging.OrderEventPublisher;
import org.example.orderservice.infrastructure.repository.OrderRepository;
import org.example.orderservice.mapper.OrderMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;
    private final CartService cartService;

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("User is not authenticated.");
        }
        return UUID.fromString(authentication.getName());
    }

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto orderRequestDto) {
        Order order = new Order();
        order.setCustomerId(orderRequestDto.getCustomerId());
        order.setDeliveryAddress(orderRequestDto.getDeliveryAddress());
        order.setStatus("PENDING");
        order.setPlacedAt(LocalDateTime.now());

        List<OrderLine> orderLines = orderRequestDto.getItems().stream()
                .map(itemDto -> {
                    OrderLine line = new OrderLine();
                    line.setOrder(order);
                    line.setProductId(itemDto.getProductId());
                    line.setProductTitle(itemDto.getProductTitle());
                    line.setUnitPrice(itemDto.getUnitPrice());
                    line.setQuantity(itemDto.getQuantity());
                    line.setSubtotal(itemDto.getUnitPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
                    return line;
                })
                .collect(Collectors.toList());

        order.setOrderLines(orderLines);
        order.setTotalAmount(orderLines.stream().map(OrderLine::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add));

        OrderStatusHistory initialStatus = new OrderStatusHistory();
        initialStatus.setOrder(order);
        initialStatus.setToStatus("PENDING");
        initialStatus.setChangedAt(LocalDateTime.now());
        order.setStatusHistory(List.of(initialStatus));

        Order savedOrder = orderRepository.save(order);
        publishOrderPlacedEvent(savedOrder);
        return OrderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('BUYER')") // Explicităm că doar BUYER are voie
    public OrderResponseDto checkout(CheckoutRequestDto checkoutRequest) {
        CartDto cart = cartService.getCart();
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot checkout an empty cart.");
        }

        List<OrderLineDto> orderLines = cart.getItems().stream()
                .map(cartItem -> OrderLineDto.builder()
                        .productId(cartItem.getProductId())
                        .productTitle(cartItem.getProductTitle())
                        .unitPrice(cartItem.getUnitPrice())
                        .quantity(cartItem.getQuantity())
                        .build())
                .collect(Collectors.toList());

        OrderRequestDto orderRequest = OrderRequestDto.builder()
                .customerId(getCurrentUserId())
                .deliveryAddress(checkoutRequest.getDeliveryAddress())
                .items(orderLines)
                .build();

        OrderResponseDto createdOrder = createOrder(orderRequest);
        cartService.clearCart();
        return createdOrder;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('BUYER') and @orderServiceImpl.isOrderOwner(authentication, #orderId))")
    public OrderResponseDto getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .map(OrderMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('BUYER')")
    public List<OrderResponseDto> getAllOrders() {
        UUID userId = getCurrentUserId();
        return orderRepository.findByCustomerId(userId).stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public OrderResponseDto updateOrderStatus(UUID orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        String oldStatus = order.getStatus();
        if (List.of("CANCELLED", "DELIVERED").contains(oldStatus)) {
            throw new IllegalStateException("Cannot change status of a completed or cancelled order.");
        }

        OrderStatusHistory statusUpdate = new OrderStatusHistory();
        statusUpdate.setOrder(order);
        statusUpdate.setFromStatus(oldStatus);
        statusUpdate.setToStatus(newStatus);
        statusUpdate.setChangedAt(LocalDateTime.now());

        order.getStatusHistory().add(statusUpdate);
        order.setStatus(newStatus);

        Order updatedOrder = orderRepository.save(order);
        publishOrderStatusChangedEvent(updatedOrder, oldStatus);
        return OrderMapper.toDto(updatedOrder);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('BUYER') and @orderServiceImpl.isOrderOwner(authentication, #orderId)")
    public OrderResponseDto cancelOrder(UUID orderId) {
        return updateOrderStatusForOwner(orderId, "CANCELLED");
    }

    public boolean isOrderOwner(Authentication authentication, UUID orderId) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        return orderRepository.findById(orderId)
                .map(order -> order.getCustomerId().equals(currentUserId))
                .orElse(false);
    }

    private OrderResponseDto updateOrderStatusForOwner(UUID orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        if (!order.getCustomerId().equals(getCurrentUserId())) {
            throw new AccessDeniedException("User is not the owner of this order.");
        }
        return updateOrderStatus(orderId, newStatus);
    }

    private void publishOrderPlacedEvent(Order order) {
        List<OrderPlacedEvent.OrderItem> eventItems = order.getOrderLines().stream()
                .map(line -> new OrderPlacedEvent.OrderItem(line.getProductId(), line.getQuantity()))
                .collect(Collectors.toList());

        OrderPlacedEvent event = new OrderPlacedEvent(order.getId(), order.getCustomerId(), order.getTotalAmount(), order.getPlacedAt(), eventItems);
        orderEventPublisher.publishOrderPlacedEvent(event);
    }

    private void publishOrderStatusChangedEvent(Order order, String oldStatus) {
        OrderStatusChangedEvent event = new OrderStatusChangedEvent(order.getId(), order.getCustomerId(), oldStatus, order.getStatus(), LocalDateTime.now());
        orderEventPublisher.publishOrderStatusChangedEvent(event);
    }
}
