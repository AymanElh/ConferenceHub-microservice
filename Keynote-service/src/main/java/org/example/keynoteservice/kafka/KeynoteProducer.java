package org.example.keynoteservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeynoteProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${application.kafka.topic.keynote:keynote-topic}")
    private String keynoteTopic;

    /**
     * Publishes a {@link KeynoteWelcomeEvent} to the Kafka keynote topic so that
     * the notification-service can consume it and send a welcome e-mail.
     *
     * @param event the welcome event built from the newly created keynote
     */
    public void sendWelcomeEvent(KeynoteWelcomeEvent event) {
        log.info("Publishing KeynoteWelcomeEvent for keynote id={} email={}",
                event.getKeynoteId(), event.getEmail());

        Message<KeynoteWelcomeEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, keynoteTopic)
                .build();

        kafkaTemplate.send(message);

        log.info("KeynoteWelcomeEvent published successfully to topic '{}'", keynoteTopic);
    }
}
