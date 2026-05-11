package org.example.orderservice.service;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.dto.*;
import org.example.orderservice.dto.event.OrderPlacedEvent;
import org.example.orderservice.dto.event.OrderStatusChangedEvent;
import org.example.orderservice.dto.event.OrderStockReserveMessage;
import org.example.orderservice.dto.event.SellerSalesMessage;
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
import java.util.Map;
import java.util.Set;
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
                    line.setSellerId(itemDto.getSellerId());
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

        for (OrderLine line : orderLines) {
            orderEventPublisher.sendStockReserveCommand(
                new OrderStockReserveMessage(UUID.fromString(line.getProductId()), line.getQuantity(), savedOrder.getId())
            );
        }

        Map<UUID, BigDecimal> salesPerSeller = orderLines.stream()
                .filter(line -> line.getSellerId() != null)
                .collect(Collectors.groupingBy(
                        OrderLine::getSellerId,
                        Collectors.reducing(BigDecimal.ZERO, OrderLine::getSubtotal, BigDecimal::add)
                ));

        salesPerSeller.forEach((sellerId, total) -> {
            orderEventPublisher.sendSellerSalesUpdate(new SellerSalesMessage(sellerId, total.doubleValue()));
        });

        return OrderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('BUYER')")
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
                        .sellerId(cartItem.getSellerId())
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
    @PreAuthorize("hasRole('ADMIN') or (hasRole('BUYER') and @orderServiceImpl.isOrderOwner(authentication, #orderId)) or (hasRole('SELLER') and @orderServiceImpl.isSellerOfOrder(authentication, #orderId))")
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
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderResponseDto> getAllOrdersForAdmin() {
        return orderRepository.findAll().stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('SELLER')")
    public List<OrderResponseDto> getOrdersForSeller() {
        UUID sellerId = getCurrentUserId();
        return orderRepository.findOrdersBySellerId(sellerId).stream()
                .map(OrderMapper::toDto)
               .peek(dto -> dto.setItems(
                        dto.getItems().stream()
                                .filter(item -> sellerId.equals(item.getSellerId()))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    public OrderResponseDto updateOrderStatus(UUID orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            UUID currentUserId = getCurrentUserId();
            boolean isSellerForOrder = order.getOrderLines().stream()
                    .anyMatch(line -> currentUserId.equals(line.getSellerId()));

            if (!isSellerForOrder) {
                throw new AccessDeniedException("You are not authorized to update this order's status.");
            }
        }

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

        if ("CANCELLED".equals(newStatus)) {
            for (OrderLine line : order.getOrderLines()) {
                orderEventPublisher.sendStockReleaseCommand(
                    new OrderStockReserveMessage(UUID.fromString(line.getProductId()), line.getQuantity(), order.getId())
                );
            }

            Map<UUID, BigDecimal> salesPerSeller = order.getOrderLines().stream()
                    .filter(line -> line.getSellerId() != null)
                    .collect(Collectors.groupingBy(
                            OrderLine::getSellerId,
                            Collectors.reducing(BigDecimal.ZERO, OrderLine::getSubtotal, BigDecimal::add)
                    ));

            salesPerSeller.forEach((sellerId, total) -> {
                orderEventPublisher.sendSellerSalesUpdate(new SellerSalesMessage(sellerId, -total.doubleValue()));
            });
        }

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

    public boolean isSellerOfOrder(Authentication authentication, UUID orderId) {
        UUID currentUserId = UUID.fromString(authentication.getName());
        return orderRepository.findById(orderId)
                .map(order -> order.getOrderLines().stream()
                        .anyMatch(line -> currentUserId.equals(line.getSellerId())))
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

        Set<UUID> sellerIds = order.getOrderLines().stream()
                .map(OrderLine::getSellerId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        OrderPlacedEvent event = new OrderPlacedEvent(order.getId(), order.getCustomerId(), order.getTotalAmount(), order.getPlacedAt(), eventItems, sellerIds);
        orderEventPublisher.publishOrderPlacedEvent(event);
    }

    private void publishOrderStatusChangedEvent(Order order, String oldStatus) {
        OrderStatusChangedEvent event = new OrderStatusChangedEvent(order.getId(), order.getCustomerId(), oldStatus, order.getStatus(), LocalDateTime.now());
        orderEventPublisher.publishOrderStatusChangedEvent(event);
    }
}
