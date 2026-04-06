package com.conferenchub.conferenceservice.conference.kafka;

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
public class ConferenceEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${application.kafka.topic.conference:conference-events}")
    private String conferenceTopic;

    public void publishConferenceCreated(ConferenceCreatedEvent event) {
        log.info("Publishing ConferenceCreatedEvent for conference id={} title={}",
                event.conferenceId(), event.titre());

        Message<ConferenceCreatedEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, conferenceTopic)
                .build();

        kafkaTemplate.send(message);

        log.info("ConferenceCreatedEvent published successfully to topic '{}'", conferenceTopic);
    }

    public void publishConferenceStatusChanged(ConferenceStatusChangedEvent event) {
        log.info("Publishing ConferenceStatusChangedEvent for conference id={} title={} status={}",
                event.conferenceId(), event.title(), event.status());

        Message<ConferenceStatusChangedEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, conferenceTopic)
                .build();

        kafkaTemplate.send(message);

        log.info("ConferenceStatusChangedEvent published successfully to topic '{}'", conferenceTopic);
    }
}