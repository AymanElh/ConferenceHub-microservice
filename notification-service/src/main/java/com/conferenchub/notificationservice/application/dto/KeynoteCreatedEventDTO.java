package com.conferenchub.notificationservice.application.dto;

public record KeynoteCreatedEventDTO(
        Long keynoteId,
        String nom,
        String prenom,
        String email,
        String fonction,
        String welcomeMessage
) {
}
