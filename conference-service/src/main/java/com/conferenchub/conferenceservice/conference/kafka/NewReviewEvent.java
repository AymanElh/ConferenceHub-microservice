package com.conferenchub.conferenceservice.conference.kafka;

import java.time.LocalDateTime;

public record NewReviewEvent(
        String eventType,
        LocalDateTime timestamp,
        Long reviewId,
        Long conferenceId,
        Integer stars,
        String text,
        String authorEmail
) {
}
