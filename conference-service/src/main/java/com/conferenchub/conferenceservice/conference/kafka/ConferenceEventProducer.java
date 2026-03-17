package com.conferenchub.conferenceservice.conference.kafka;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConferenceEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private String conferenceTopic;

}