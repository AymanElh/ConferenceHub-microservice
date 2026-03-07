package com.conferenchub.notificationservice.application.dto;

import java.time.LocalDateTime;

public record ConferenceStatusChangedEventDTO(
        String eventType,
        LocalDateTime timestamp,
        Long conferenceId,
        String ancienStatut,
        String nouveauStatut
) {
}
