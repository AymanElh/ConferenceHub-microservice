package com.conferenchub.conferenceservice.conference.kafka;

import com.conferenchub.conferenceservice.conference.entity.Conference;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ConferenceEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String CONFERENCE_TOPIC = "conference-events";

    public void publishConferenceCreated(Conference conference){

        Map<String,Object> event = new HashMap<>();

        event.put("eventType","CONFERENCE_CREATED");
        event.put("timestamp", LocalDateTime.now());
        event.put("conferenceId",conference.getId());
        event.put("title",conference.getTitle());
        event.put("type",conference.getType());
        event.put("date",conference.getDate());
        event.put("status",conference.getStatus());

        kafkaTemplate.send(CONFERENCE_TOPIC,event);
    }

    public void publishConferenceStatusChanged(Conference conference,String oldStatus){

        Map<String,Object> event = new HashMap<>();

        event.put("eventType","CONFERENCE_STATUS_CHANGED");
        event.put("timestamp", LocalDateTime.now());
        event.put("conferenceId",conference.getId());
        event.put("oldStatus",oldStatus);
        event.put("newStatus",conference.getStatus());

        kafkaTemplate.send(CONFERENCE_TOPIC,event);
    }

}