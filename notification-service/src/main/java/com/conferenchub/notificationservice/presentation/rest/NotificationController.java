package com.conferenchub.notificationservice.presentation.rest;

import com.conferenchub.notificationservice.domain.model.Notification;
import com.conferenchub.notificationservice.domain.model.NotificationStatus;
import com.conferenchub.notificationservice.domain.repository.NotificationRepository;
import com.conferenchub.notificationservice.application.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @GetMapping
    public Page<Notification> getAllNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable);
    }

    @GetMapping(params = "email")
    public Page<Notification> getNotificationsByEmail(@RequestParam String email, Pageable pageable) {
        return notificationRepository.findByDestinataire(email, pageable);
    }

    @GetMapping(params = "conferenceId")
    public Page<Notification> getNotificationsByConferenceId(@RequestParam Long conferenceId, Pageable pageable) {
        return notificationRepository.findByReferenceId(conferenceId, pageable);
    }

    @GetMapping(params = "statut=ECHOUEE")
    public List<Notification> getFailedNotifications() {
        return notificationRepository.findByStatut(NotificationStatus.ECHOUEE);
    }

    @PostMapping("/retry/{id}")
    public ResponseEntity<String> retryNotification(@PathVariable String id) {
        try {
            notificationService.retryFailedNotification(id);
            return ResponseEntity.ok("Retry successful");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
