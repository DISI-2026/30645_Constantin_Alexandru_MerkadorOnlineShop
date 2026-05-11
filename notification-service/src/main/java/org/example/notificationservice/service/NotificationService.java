package org.example.notificationservice.service;

import org.example.notificationservice.dto.NotificationDto;
import org.example.notificationservice.dto.event.OrderPlacedEvent;
import org.example.notificationservice.dto.event.OrderStatusChangedEvent;
import org.example.notificationservice.dto.event.ReviewNotificationEvent;
import org.example.notificationservice.infrastructure.entity.NotificationType;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface NotificationService {

    void processOrderPlaced(OrderPlacedEvent event);

    void processOrderStatusChanged(OrderStatusChangedEvent event);

    void processReviewPosted(ReviewNotificationEvent event);

    Page<NotificationDto> getUserNotifications(UUID userId, int page, int size);

    long getUnreadCount(UUID userId);

    NotificationDto markAsRead(UUID notificationId, UUID userId);

    void markAllAsRead(UUID userId);

    void deleteNotification(UUID notificationId, UUID userId);

    void sendToAdmins(String message, NotificationType type);
}
