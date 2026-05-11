package org.example.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.dto.NotificationDto;
import org.example.notificationservice.dto.event.OrderPlacedEvent;
import org.example.notificationservice.dto.event.OrderStatusChangedEvent;
import org.example.notificationservice.dto.event.ReviewNotificationEvent;
import org.example.notificationservice.infrastructure.entity.Notification;
import org.example.notificationservice.infrastructure.entity.NotificationType;
import org.example.notificationservice.infrastructure.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    // universal UUID that addresses all admins
    public static final UUID ADMIN_GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Override
    @Transactional
    public void processOrderPlaced(OrderPlacedEvent event) {
        String title = "Order Placed Successfully";
        String message = String.format(
                "Your order #%s has been placed! Total: $%.2f",
                event.getOrderId(), event.getTotalAmount()
        );

        Notification notification = Notification.builder()
                .userId(event.getCustomerId())
                .type(NotificationType.ORDER_PLACED)
                .title(title)
                .message(message)
                .payload(event.getOrderId().toString())
                .build();

        notification = notificationRepository.save(notification);
        pushToUser(event.getCustomerId(), notification);
        log.info("Processed ORDER_PLACED event for user {}", event.getCustomerId());
    }

    @Override
    @Transactional
    public void processOrderStatusChanged(OrderStatusChangedEvent event) {
        String title = "Order Status Updated";
        String message = String.format(
                "Your order #%s status changed from %s to %s.",
                event.getOrderId(), event.getOldStatus(), event.getNewStatus()
        );

        Notification notification = Notification.builder()
                .userId(event.getCustomerId())
                .type(NotificationType.ORDER_STATUS_CHANGED)
                .title(title)
                .message(message)
                .payload(event.getOrderId().toString())
                .build();

        notification = notificationRepository.save(notification);
        pushToUser(event.getCustomerId(), notification);
        log.info("Processed ORDER_STATUS_CHANGED event for user {}", event.getCustomerId());
    }

    @Override
    @Transactional
    public void processReviewPosted(ReviewNotificationEvent event) {
        String title = "Review Submitted";
        String message = String.format(
                "Your review for product #%s has been submitted with a %d-star rating.",
                event.getProductId(), event.getRating()
        );

        Notification notification = Notification.builder()
                .userId(event.getCustomerId())
                .type(NotificationType.REVIEW_POSTED)
                .title(title)
                .message(message)
                .payload(event.getProductId() != null ? event.getProductId().toString() : null)
                .build();

        notification = notificationRepository.save(notification);
        pushToUser(event.getCustomerId(), notification);
        log.info("Processed REVIEW_POSTED event for user {}", event.getCustomerId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> getUserNotifications(UUID userId, int page, int size) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    @Transactional
    public NotificationDto markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notification.setRead(true);
        return toDto(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteNotification(UUID notificationId, UUID userId) {
        notificationRepository.deleteByIdAndUserId(notificationId, userId);
    }

    @Override
    public void sendToAdmins(String messageText, NotificationType type) {
        // Save notification to DB
        Notification notification = Notification.builder()
                .userId(ADMIN_GROUP_ID)
                .message(messageText)
                .type(type)
                .title("Shop Verification Request")
                .read(false)
                .createdAt(LocalDateTime.now())
                .payload(null)
                .build();

        notification = notificationRepository.save(notification);

        NotificationDto dto = new NotificationDto(
                notification.getId(),
                notification.getUserId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getPayload()
        );

        // send it through the admin's channel
        messagingTemplate.convertAndSend("/topic/admins", dto);
    }

    private void pushToUser(UUID userId, Notification notification) {
        try {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications",
                    toDto(notification)
            );
        } catch (Exception e) {
            log.warn("Could not push WebSocket notification to user {}: {}", userId, e.getMessage());
        }
    }

    private NotificationDto toDto(Notification n) {
        return NotificationDto.builder()
                .id(n.getId())
                .userId(n.getUserId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .payload(n.getPayload())
                .build();
    }
}
