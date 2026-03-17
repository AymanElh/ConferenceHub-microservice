package com.conferenchub.notificationservice.application.service;

import com.conferenchub.notificationservice.domain.model.Notification;
import com.conferenchub.notificationservice.domain.model.NotificationStatus;
import com.conferenchub.notificationservice.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    public void processAndSaveNotification(Notification notification) {
        log.info("Processing notification for: {}", notification.getDestinataire());
        
        notification.setTentatives(notification.getTentatives() + 1);
        notification.setDateEnvoi(LocalDateTime.now());
        
        try {
            // Emulate email sending logic
            sendEmail(notification.getDestinataire(), notification.getSujet(), notification.getContenu());
            notification.setStatut(NotificationStatus.ENVOYEE);
            log.info("Notification successfully sent to {}", notification.getDestinataire());
        } catch (Exception e) {
            log.error("Failed to send notification to {}", notification.getDestinataire(), e);
            notification.setStatut(NotificationStatus.ECHOUEE);
            notification.setErreurMessage(e.getMessage());
        }
        
        notificationRepository.save(notification);
    }

    private void sendEmail(String to, String subject, String content) {
        if (to == null || to.trim().isEmpty()) {
            throw new IllegalArgumentException("Destinataire (to) address is missing");
        }
        
        log.info("Sending email via JavaMailSender to: {}, Subject: {}", to, subject);
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        message.setFrom("noreply@conferencehub.com");
        
        mailSender.send(message);
    }

    public void retryFailedNotification(String id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found for id: " + id));
        
        if (notification.getStatut() != NotificationStatus.ECHOUEE) {
            throw new IllegalStateException("Only failed notifications can be retried");
        }
        
        log.info("Retrying failed notification id: {}", id);
        processAndSaveNotification(notification);
    }
}
