package com.conferenchub.conferenceservice.conference.kafka.producer;
    
import com.conferenchub.conferenceservice.conference.kafka.event.NewInscriptionEvent;

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
public class NewInscriptionEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${application.kafka.topic.inscription:inscription-events}")
    private String conferenceTopic;

    public void publishNewInscription(NewInscriptionEvent inscriptionEvent) {
        log.info("Publishing NewInscriptionEvent for conference id={} participantEmail={}",
                inscriptionEvent.conferenceId(), inscriptionEvent.participantEmail());

        Message<NewInscriptionEvent> message = MessageBuilder
                .withPayload(inscriptionEvent)
                .setHeader(KafkaHeaders.TOPIC, conferenceTopic)
                .build();

        kafkaTemplate.send(message);

        log.info("ConferenceCreatedEvent published successfully to topic '{}'", conferenceTopic);
    }
}
