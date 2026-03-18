package com.conferenchub.notificationservice.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ConferenceCreatedEventDTO(
        String eventType,
        LocalDateTime timestamp,
        Long conferenceId,
        String titre,
        String type,
        LocalDate date,
        String statut
) {
}
