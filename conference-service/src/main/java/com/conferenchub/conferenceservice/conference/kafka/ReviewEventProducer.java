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
public class ReviewEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${application.kafka.topic.review:review-events}")
    private String reviewTopic;

    public void publishReviewCreated(NewReviewEvent reviewEvent) {
        log.info("Publishing NewReviewEvent for conference id={} reviewId={}",
                reviewEvent.conferenceId(), reviewEvent.reviewId());

        Message<NewReviewEvent> message = MessageBuilder
                .withPayload(reviewEvent)
                .setHeader(KafkaHeaders.TOPIC, reviewTopic)
                .build();

        kafkaTemplate.send(message);

        log.info("NewReviewEvent published successfully to topic '{}'", reviewTopic);
    }
}
