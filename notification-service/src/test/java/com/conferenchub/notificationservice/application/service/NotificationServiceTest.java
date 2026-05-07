package com.conferenchub.notificationservice.application.service;

import com.conferenchub.notificationservice.domain.model.EventType;
import com.conferenchub.notificationservice.domain.model.Notification;
import com.conferenchub.notificationservice.domain.model.NotificationStatus;
import com.conferenchub.notificationservice.domain.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NotificationService service;

    @Test
    void processAndSaveNotification_whenAlreadySent_skips() {
        Notification n = new Notification();
        n.setReferenceId(1L);
        n.setTypeEvenement(EventType.CONFERENCE_CREATED);
        n.setStatut(NotificationStatus.ENVOYEE);
        n.setDestinataire("a@example.com");
        n.setTentatives(0);

        when(notificationRepository.existsByReferenceIdAndTypeEvenementAndStatut(
                1L, EventType.CONFERENCE_CREATED, NotificationStatus.ENVOYEE)).thenReturn(true);

        service.processAndSaveNotification(n);

        verify(notificationRepository, never()).save(any());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void processAndSaveNotification_sendsMail_andMarksSent() {
        Notification n = new Notification();
        n.setReferenceId(2L);
        n.setTypeEvenement(EventType.CONFERENCE_CREATED);
        n.setDestinataire("a@example.com");
        n.setSujet("S");
        n.setContenu("C");
        n.setTentatives(0);

        when(notificationRepository.existsByReferenceIdAndTypeEvenementAndStatut(
                2L, EventType.CONFERENCE_CREATED, NotificationStatus.ENVOYEE)).thenReturn(false);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        service.processAndSaveNotification(n);

        verify(mailSender).send(any(SimpleMailMessage.class));
        verify(notificationRepository, org.mockito.Mockito.times(2)).save(any(Notification.class));
    }

    @Test
    void retryFailedNotification_whenNotFailed_throws() {
        Notification n = new Notification();
        n.setId("1");
        n.setStatut(NotificationStatus.ENVOYEE);
        when(notificationRepository.findById("1")).thenReturn(Optional.of(n));

        assertThatThrownBy(() -> service.retryFailedNotification("1"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void processAndSaveNotification_whenToMissing_marksFailed() {
        Notification n = new Notification();
        n.setReferenceId(3L);
        n.setTypeEvenement(EventType.CONFERENCE_CREATED);
        n.setDestinataire(" ");
        n.setSujet("S");
        n.setContenu("C");
        n.setTentatives(0);

        when(notificationRepository.existsByReferenceIdAndTypeEvenementAndStatut(
                3L, EventType.CONFERENCE_CREATED, NotificationStatus.ENVOYEE)).thenReturn(false);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        service.processAndSaveNotification(n);

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        verify(notificationRepository, org.mockito.Mockito.times(2)).save(any(Notification.class));
    }
}
