package org.example.keynoteservice.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published to Kafka when a new Keynote speaker is created.
 * The notification-service listens on "keynote-topic" and sends a welcome e-mail.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KeynoteWelcomeEvent {
    private Long   keynoteId;
    private String nom;
    private String prenom;
    private String email;
    private String fonction;
    private String welcomeMessage;
}

