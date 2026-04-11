package com.conferenchub.notificationservice.application.service;

import com.conferenchub.notificationservice.domain.model.EventType;
import com.conferenchub.notificationservice.domain.model.Notification;
import com.conferenchub.notificationservice.domain.model.NotificationStatus;
import com.conferenchub.notificationservice.domain.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NotificationService notificationService;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = Notification.builder()
                .id("test-id")
                .destinataire("user@example.com")
                .sujet("Test Subject")
                .contenu("Test Content")
                .typeEvenement(EventType.CONFERENCE_CREATED)
                .referenceId(123L)
                .tentatives(0)
                .build();
    }

    @Test
    void processAndSaveNotification_ShouldSendEmailAndMarkAsSent() {
        // Given
        when(notificationRepository.existsByReferenceIdAndTypeEvenementAndStatut(any(), any(), any()))
                .thenReturn(false);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // When
        notificationService.processAndSaveNotification(notification);

        // Then
        verify(mailSender).send(any(SimpleMailMessage.class));
        verify(notificationRepository, times(2)).save(notification);
        assertThat(notification.getStatut()).isEqualTo(NotificationStatus.ENVOYEE);
    }

    @Test
    void processAndSaveNotification_ShouldSkip_WhenAlreadySent() {
        // Given
        when(notificationRepository.existsByReferenceIdAndTypeEvenementAndStatut(123L, EventType.CONFERENCE_CREATED, NotificationStatus.ENVOYEE))
                .thenReturn(true);

        // When
        notificationService.processAndSaveNotification(notification);

        // Then
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void processAndSaveNotification_ShouldMarkAsFailed_WhenMailSenderFails() {
        // Given
        when(notificationRepository.existsByReferenceIdAndTypeEvenementAndStatut(any(), any(), any()))
                .thenReturn(false);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        doThrow(new RuntimeException("SMTP Server Down")).when(mailSender).send(any(SimpleMailMessage.class));

        // When
        notificationService.processAndSaveNotification(notification);

        // Then
        assertThat(notification.getStatut()).isEqualTo(NotificationStatus.ECHOUEE);
        assertThat(notification.getErreurMessage()).isEqualTo("SMTP Server Down");
        verify(notificationRepository, times(2)).save(notification);
    }

    @Test
    void retryFailedNotification_ShouldProcessAgain_WhenStatusIsFailed() {
        // Given
        notification.setStatut(NotificationStatus.ECHOUEE);
        when(notificationRepository.findById("test-id")).thenReturn(Optional.of(notification));
        when(notificationRepository.existsByReferenceIdAndTypeEvenementAndStatut(any(), any(), any()))
                .thenReturn(false);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // When
        notificationService.retryFailedNotification("test-id");

        // Then
        verify(mailSender).send(any(SimpleMailMessage.class));
        assertThat(notification.getStatut()).isEqualTo(NotificationStatus.ENVOYEE);
    }

    @Test
    void retryFailedNotification_ShouldThrowException_WhenStatusIsNotFailed() {
        // Given
        notification.setStatut(NotificationStatus.ENVOYEE);
        when(notificationRepository.findById("test-id")).thenReturn(Optional.of(notification));

        // When / Then
        assertThatThrownBy(() -> notificationService.retryFailedNotification("test-id"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only failed notifications can be retried");
    }
}
