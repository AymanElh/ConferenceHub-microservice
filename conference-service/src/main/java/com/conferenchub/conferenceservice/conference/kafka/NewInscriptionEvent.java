package com.conferenchub.conferenceservice.conference.kafka;

import java.time.LocalDateTime;

public record NewInscriptionEvent(
        String eventType,
        LocalDateTime timestamp,
        Long inscriptionId,
        Long conferenceId,
        String participantEmail,
        String participantNom
) {
}
