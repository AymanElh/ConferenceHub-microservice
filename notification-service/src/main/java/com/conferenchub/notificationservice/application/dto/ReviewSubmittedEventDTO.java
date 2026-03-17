package com.conferenchub.notificationservice.application.dto;

import java.time.LocalDateTime;

public record ReviewSubmittedEventDTO(
        String eventType,
        LocalDateTime timestamp,
        Long reviewId,
        Long conferenceId,
        Integer stars,
        String auteurEmail
) {
}
