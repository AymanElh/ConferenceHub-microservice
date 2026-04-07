package com.conferenchub.conferenceservice.conference.kafka.event;

import java.time.LocalDateTime;
import java.util.List;

public record ConferenceStatusChangedEvent(
        String eventType,
        LocalDateTime timestamp,
        Long conferenceId,
        String title,
        String status,
        List<String> participantEmails
) {
}
