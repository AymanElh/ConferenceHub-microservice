package com.conferenchub.notificationservice.application.dto;

import java.time.LocalDateTime;

import java.util.List;

public record ReviewSubmittedEventDTO(
        String eventType,
        LocalDateTime timestamp,
        Long reviewId,
        Long conferenceId,
        Integer stars,
        String authorEmail,
        List<String> keynoteEmails
) {
}
