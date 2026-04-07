package com.conferenchub.notificationservice.infrastructure.messaging;

import com.conferenchub.notificationservice.application.dto.*;
import com.conferenchub.notificationservice.domain.model.EventType;
import com.conferenchub.notificationservice.domain.model.Notification;
import com.conferenchub.notificationservice.application.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventHandler {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Value("${application.notification.managers:}")
    private String managerEmailsCsv;

    @KafkaListener(
            topics = "${application.kafka.topic.keynote:keynote-events}",
            groupId = "notification-group"
    )
    public void handleKeynoteCreated(KeynoteCreatedEventDTO event) {
        log.info("Received KeynoteCreatedEvent for: {}", event.email());
        Notification notification = Notification.builder()
                .destinataire(event.email())
                .sujet("Bienvenue en tant que Keynote")
                .contenu(String.format("Bonjour %s %s, \nBienvenue sur ConferenceHub !", event.prenom(), event.nom()))
                .typeEvenement(EventType.KEYNOTE_CREATED)
                .referenceId(event.keynoteId())
                .build();

        log.info("Keynote create notification: {}", notification);
        notificationService.processAndSaveNotification(notification);
    }

    @KafkaListener(topics = "${application.kafka.topic.conference:conference-events}", groupId = "notification-group")
    public void handleConferenceEvents(ConsumerRecord<String, String> record) {
        String messageJson = record.value();
        try {
            // First, peek at the eventType
            var node = objectMapper.readTree(messageJson);
            log.debug("Conference event received: {}", messageJson);
            String eventType = node.get("eventType").asText();

            if ("CONFERENCE_CREATED".equals(eventType)) {
                ConferenceCreatedEventDTO event = objectMapper.readValue(messageJson, ConferenceCreatedEventDTO.class);
                processConferenceCreated(event);
            } else if ("CONFERENCE_STATUS_CHANGED".equals(eventType)) {
                ConferenceStatusChangedEventDTO event = objectMapper.readValue(messageJson, ConferenceStatusChangedEventDTO.class);
                processConferenceStatusChanged(event);
            } else {
                log.warn("Unknown conference event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Failed to parse conference event: {}", messageJson, e);
        }
    }

    private void processConferenceCreated(ConferenceCreatedEventDTO event) {
        log.info("Processing ConferenceCreatedEvent for conferenceId: {}", event.conferenceId());
        List<String> managerEmails = parseManagerEmails();
        if (managerEmails.isEmpty()) {
            log.warn("No manager emails configured; skipping conference notification.");
            return;
        }

        for (String managerEmail : managerEmails) {
            Notification notification = Notification.builder()
                    .destinataire(managerEmail)
                    .sujet("Nouvelle conférence créée")
                    .contenu(String.format("Une nouvelle conférence intitulée '%s' a été créée pour le %s.",
                            event.titre(), event.date()))
                    .typeEvenement(EventType.CONFERENCE_CREATED)
                    .referenceId(event.conferenceId())
                    .build();

            log.info("Conference create notification: {}", notification);
            notificationService.processAndSaveNotification(notification);
        }
    }

    private void processConferenceStatusChanged(ConferenceStatusChangedEventDTO event) {
        log.info("Processing ConferenceStatusChangedEvent for conferenceId: {} with status: {}", event.conferenceId(), event.status());

        List<String> participants = event.participantEmails();
        if (participants == null || participants.isEmpty()) {
            log.warn("No participants to notify for conference {}", event.conferenceId());
            return;
        }

        String subject;
        String content;
        EventType eventType;

        switch (event.status()) {
            case "EN_COURS" -> {
                subject = "Rappel: Votre conférence commence !";
                content = String.format("La conférence '%s' a maintenant commencé (EN COURS). Ne la manquez pas !", event.title());
                eventType = EventType.CONFERENCE_STATUS_CHANGED;
            }
            case "ANNULE" -> {
                subject = "Conférence annulée";
                content = String.format("Nous sommes au regret de vous informer que la conférence '%s' a été annulée.", event.title());
                eventType = EventType.CONFERENCE_CANCELLED;
            }
            case "TERMINE" -> {
                subject = "Merci d'avoir participé !";
                content = String.format("La conférence '%s' est maintenant terminée. Merci pour votre participation !", event.title());
                eventType = EventType.CONFERENCE_COMPLETED;
            }
            default -> {
                log.warn("Unhandled status: {}", event.status());
                return;
            }
        }

        for (String email : participants) {
            Notification notification = Notification.builder()
                    .destinataire(email)
                    .sujet(subject)
                    .contenu(content)
                    .typeEvenement(eventType)
                    .referenceId(event.conferenceId())
                    .build();

            log.debug("Sending {} notification to {}", event.status(), email);
            notificationService.processAndSaveNotification(notification);
        }
    }

    private List<String> parseManagerEmails() {
        if (managerEmailsCsv == null || managerEmailsCsv.isBlank()) {
            return List.of();
        }

        return Arrays.stream(managerEmailsCsv.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toList());
    }

    @KafkaListener(topics = "review-events", groupId = "notification-group")
    public void handleReviewSubmitted(ReviewSubmittedEventDTO event) {
        log.info("Received ReviewSubmittedEvent for conferenceId: {}", event.conferenceId());
        Notification notification = Notification.builder()
                .destinataire("keynote-placeholder@conferencehub.com") // We would need keynote email
                .sujet("Nouvel avis sur votre conférence")
                .contenu(String.format("Vous avez reçu un avis de %s avec une note de %d.", event.auteurEmail(), event.stars()))
                .typeEvenement(EventType.REVIEW_SUBMITTED)
                .referenceId(event.reviewId())
                .build();
        notificationService.processAndSaveNotification(notification);
    }

    @KafkaListener(topics = "inscription-events", groupId = "notification-group")
    public void handleParticipantRegistered(ParticipantRegisteredEventDTO event) {
        log.info("Received ParticipantRegisteredEvent for email: {}", event.participantEmail());
        Notification notification = Notification.builder()
                .destinataire(event.participantEmail())
                .sujet("Confirmation d'inscription")
                .contenu(String.format("Bonjour %s,\nVotre inscription à la conférence %d est confirmée.", event.participantNom(), event.conferenceId()))
                .typeEvenement(EventType.PARTICIPANT_REGISTERED)
                .referenceId(event.inscriptionId())
                .build();
        notificationService.processAndSaveNotification(notification);
    }
}
