package org.example.notificationservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.notificationservice.dto.NotificationDto;
import org.example.notificationservice.dto.event.VerificationReqEvent;
import org.example.notificationservice.infrastructure.entity.NotificationType;
import org.example.notificationservice.service.NotificationService;
import org.example.notificationservice.service.NotificationServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationDto>> getUserNotifications(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                notificationService.getUserNotifications(UUID.fromString(userId), page, size)
        );
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal String userId) {
        long count = notificationService.getUnreadCount(UUID.fromString(userId));
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<NotificationDto>> getAdminNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        // an admin has access to the messages sent to ADMIN_GROUP_ID
        return ResponseEntity.ok(
                notificationService.getUserNotifications(NotificationServiceImpl.ADMIN_GROUP_ID, page, size)
        );
    }

    @GetMapping("/admin/unread-count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getAdminUnreadCount() {
        long count = notificationService.getUnreadCount(NotificationServiceImpl.ADMIN_GROUP_ID);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PostMapping("/admin/verification-request")
    public ResponseEntity<Void> requestVerification(
            @RequestBody VerificationReqEvent request,
            @AuthenticationPrincipal String userId) {

        // Construct the message
        String message = String.format("Shop '%s' requested verification for categories: %s. Message: '%s'",
                request.getShopName(),
                request.getCategories(),
                request.getMessage());

        notificationService.sendToAdmins(message, NotificationType.VERIFICATION_REQUEST);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationDto> markAsRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(
                notificationService.markAsRead(id, UUID.fromString(userId))
        );
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal String userId) {
        notificationService.markAllAsRead(UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/admin/{id}/read")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationDto> markAdminNotificationAsRead(@PathVariable UUID id) {
        return ResponseEntity.ok(
                notificationService.markAsRead(id, NotificationServiceImpl.ADMIN_GROUP_ID)
        );
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteAdminNotification(
            @PathVariable UUID id) {
        notificationService.deleteNotification(id, NotificationServiceImpl.ADMIN_GROUP_ID);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable UUID id,
            @AuthenticationPrincipal String userId) {
        notificationService.deleteNotification(id, UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }
}
