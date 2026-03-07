package com.conferenchub.notificationservice.application.dto;

import java.time.LocalDateTime;

public record KeynoteCreatedEventDTO(
        String eventType,
        LocalDateTime timestamp,
        Long keynoteId,
        String nom,
        String prenom,
        String email,
        String fonction
) {
}
