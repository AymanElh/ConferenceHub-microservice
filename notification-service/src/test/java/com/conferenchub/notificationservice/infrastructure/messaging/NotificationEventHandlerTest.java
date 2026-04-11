package com.conferenchub.notificationservice.infrastructure.messaging;

import com.conferenchub.notificationservice.application.dto.KeynoteCreatedEventDTO;
import com.conferenchub.notificationservice.application.service.NotificationService;
import com.conferenchub.notificationservice.domain.model.Notification;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    @InjectMocks
    private NotificationEventHandler notificationEventHandler;

    @Test
    void handleKeynoteCreated_ShouldProcessNotification() {
        // Given
        KeynoteCreatedEventDTO event = new KeynoteCreatedEventDTO(
                1L, "John", "Doe", "john.doe@example.com", "Speaker", "Welcome!"
        );

        // When
        notificationEventHandler.handleKeynoteCreated(event);

        // Then
        verify(notificationService).processAndSaveNotification(any(Notification.class));
    }

    @Test
    void handleConferenceEvents_ShouldProcessConferenceCreated() throws Exception {
        // Given
        String json = "{" +
                "\"eventType\":\"CONFERENCE_CREATED\"," +
                "\"timestamp\":\"2024-10-10T10:00:00\"," +
                "\"conferenceId\":1," +
                "\"titre\":\"Java One\"," +
                "\"type\":\"ACADEMIC\"," +
                "\"date\":\"2024-10-10\"," +
                "\"statut\":\"PLANNED\"" +
                "}";
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "key", json);
        ReflectionTestUtils.setField(notificationEventHandler, "managerEmailsCsv", "admin@example.com");

        // When
        notificationEventHandler.handleConferenceEvents(record);

        // Then
        verify(notificationService, atLeastOnce()).processAndSaveNotification(any(Notification.class));
    }

    @Test
    void handleConferenceEvents_ShouldProcessStatusChanged() throws Exception {
        // Given
        String json = "{" +
                "\"eventType\":\"CONFERENCE_STATUS_CHANGED\"," +
                "\"timestamp\":\"2024-10-10T10:00:00\"," +
                "\"conferenceId\":1," +
                "\"title\":\"Java One\"," +
                "\"status\":\"EN_COURS\"," +
                "\"participantEmails\":[\"user@example.com\"]" +
                "}";
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "key", json);

        // When
        notificationEventHandler.handleConferenceEvents(record);

        // Then
        verify(notificationService).processAndSaveNotification(any(Notification.class));
    }

    @Test
    void handleConferenceEvents_ShouldIgnoreUnknownType() {
        // Given
        String json = "{\"eventType\":\"UNKNOWN_TYPE\"}";
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0, "key", json);

        // When
        notificationEventHandler.handleConferenceEvents(record);

        // Then
        verify(notificationService, never()).processAndSaveNotification(any());
    }
}
