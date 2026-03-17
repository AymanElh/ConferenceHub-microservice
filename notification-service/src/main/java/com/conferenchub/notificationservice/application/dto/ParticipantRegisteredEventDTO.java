package com.conferenchub.notificationservice.application.dto;

import java.time.LocalDateTime;

public record ParticipantRegisteredEventDTO(
        String eventType,
        LocalDateTime timestamp,
        Long inscriptionId,
        Long conferenceId,
        String participantEmail,
        String participantNom
) {
}
