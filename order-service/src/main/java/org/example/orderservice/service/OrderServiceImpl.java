package org.example.orderservice.service;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.dto.OrderRequestDto;
import org.example.orderservice.dto.OrderResponseDto;
import org.example.orderservice.infrastructure.entity.Order;
import org.example.orderservice.infrastructure.entity.OrderLine;
import org.example.orderservice.infrastructure.entity.OrderStatusHistory;
import org.example.orderservice.infrastructure.repository.OrderRepository;
import org.example.orderservice.mapper.OrderMapper;
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

        BigDecimal totalAmount = orderLines.stream()
                .map(OrderLine::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);

        OrderStatusHistory initialStatus = new OrderStatusHistory();
        initialStatus.setOrder(order);
        initialStatus.setToStatus("PENDING");
        initialStatus.setChangedAt(LocalDateTime.now());
        order.setStatusHistory(List.of(initialStatus));

        Order savedOrder = orderRepository.save(order);
        return OrderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .map(OrderMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrderStatus(UUID orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        OrderStatusHistory statusUpdate = new OrderStatusHistory();
        statusUpdate.setOrder(order);
        statusUpdate.setFromStatus(order.getStatus());
        statusUpdate.setToStatus(newStatus);
        statusUpdate.setChangedAt(LocalDateTime.now());

        order.getStatusHistory().add(statusUpdate);
        order.setStatus(newStatus);

        Order updatedOrder = orderRepository.save(order);
        return OrderMapper.toDto(updatedOrder);
    }
}
