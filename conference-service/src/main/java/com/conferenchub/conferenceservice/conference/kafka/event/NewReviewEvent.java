package com.conferenchub.conferenceservice.conference.kafka.event;

import java.time.LocalDateTime;
import java.util.List;

public record NewReviewEvent(
        String eventType,
        LocalDateTime timestamp,
        Long reviewId,
        Long conferenceId,
        Integer stars,
        String text,
        String authorEmail,
        List<String> keynoteEmails
) {
}
