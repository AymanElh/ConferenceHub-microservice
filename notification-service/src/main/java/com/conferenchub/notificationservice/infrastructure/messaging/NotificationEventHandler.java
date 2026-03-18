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
    public void handleConferenceEvents(ConferenceCreatedEventDTO conferenceCreatedEventDTO) {
        log.info("Received ConferenceCreatedEvent for conferenceId: {}", conferenceCreatedEventDTO.conferenceId());
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
                            conferenceCreatedEventDTO.titre(), conferenceCreatedEventDTO.date()))
                    .typeEvenement(EventType.CONFERENCE_CREATED)
                    .referenceId(conferenceCreatedEventDTO.conferenceId())
                    .build();

            log.info("Conference create notification: {}", notification);
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
