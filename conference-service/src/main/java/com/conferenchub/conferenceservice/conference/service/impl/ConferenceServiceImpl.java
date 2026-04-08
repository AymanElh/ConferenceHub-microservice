package com.conferenchub.conferenceservice.conference.service.impl;

import com.conferenchub.conferenceservice.conference.client.KeynoteClient;
import com.conferenchub.conferenceservice.conference.dto.request.CreateConferenceRequest;
import com.conferenchub.conferenceservice.conference.dto.response.ConferenceResponse;
import com.conferenchub.conferenceservice.conference.dto.response.KeynoteResponse;
import com.conferenchub.conferenceservice.conference.entity.Conference;
import com.conferenchub.conferenceservice.conference.entity.ConferenceStatus;
import com.conferenchub.conferenceservice.conference.entity.ConferenceType;
import com.conferenchub.conferenceservice.conference.entity.Review;
import com.conferenchub.conferenceservice.conference.exception.ConferenceNotFoundException;
import com.conferenchub.conferenceservice.conference.exception.KeynoteNotFoundException;
import com.conferenchub.conferenceservice.conference.exception.KeynoteServiceUnavailableException;
import com.conferenchub.conferenceservice.conference.kafka.event.ConferenceCreatedEvent;
import com.conferenchub.conferenceservice.conference.kafka.producer.ConferenceEventProducer;
import com.conferenchub.conferenceservice.conference.kafka.event.ConferenceStatusChangedEvent;
import com.conferenchub.conferenceservice.conference.entity.Inscription;

import java.time.LocalDateTime;

import com.conferenchub.conferenceservice.conference.mapper.ConferenceMapper;
import com.conferenchub.conferenceservice.conference.repository.ConferenceRepository;
import com.conferenchub.conferenceservice.conference.service.ConferenceService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConferenceServiceImpl implements ConferenceService {

    private final ConferenceRepository conferenceRepository;
    private final ConferenceMapper mapper;
    private final ConferenceEventProducer eventProducer;
    private final KeynoteClient keynoteClient;

    @Override
    public ConferenceResponse createConference(CreateConferenceRequest request) {

        if (request.getKeynoteIds() != null && !request.getKeynoteIds().isEmpty()) {
            request.getKeynoteIds().forEach(id -> {
                try {
                    keynoteClient.getKeynoteById(id);

                } catch (FeignException.NotFound e) {
                    // Keynote doesn't exist — this is expected when user provides invalid ID
                    log.debug("Keynote with id={} not found on keynote-service", id);
                    throw new KeynoteNotFoundException(
                            "Keynote with id " + id + " not found");

                } catch (KeynoteServiceUnavailableException e) {
                    // Circuit is OPEN — the keynote-service is down
                    log.warn("Keynote-service is unavailable, cannot verify keynote id={}", id);
                    throw e;

                } catch (FeignException e) {
                    // Any other Feign exception (5xx, timeout, connection error, etc.)
                    log.error("Feign error while verifying keynote id={}: {}", id, e.getMessage());
                    throw new KeynoteServiceUnavailableException(
                            "Cannot create conference: keynote-service is temporarily unavailable. " +
                                    "Please try again in a few moments.");
                }
            });
        }

        Conference conference = mapper.toEntity(request);

        conference.setStatus(ConferenceStatus.PLANNED);
        conference.setRegisteredNumber(0);
        conference.setScore(0.0);

        Conference saved = conferenceRepository.save(conference);

        ConferenceCreatedEvent event = new ConferenceCreatedEvent(
                "CONFERENCE_CREATED",
                java.time.LocalDateTime.now(),
                saved.getId(),
                saved.getTitle(),
                saved.getType().name(),
                saved.getDate(),
                saved.getStatus().name()
        );

        log.debug("Publishing event: {}", event);
        eventProducer.publishConferenceCreated(event);

        return mapper.toResponse(saved);
    }

    @Override
    public ConferenceResponse getConferenceById(Long id) {
        Conference conference = conferenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conference not found with id: " + id));

        ConferenceResponse response = mapper.toResponse(conference);

        if (conference.getKeynoteIds() == null || conference.getKeynoteIds().isEmpty()) {
            response.setKeynotes(List.of());
            return response;
        }

        try {
            // Happy path — keynote-service is available
            List<KeynoteResponse> keynotes = conference.getKeynoteIds().stream()
                    .map(keynoteId -> {
                        try {
                            return keynoteClient.getKeynoteById(keynoteId);
                        } catch (FeignException.NotFound e) {
                            log.warn("Keynote id={} no longer exists", keynoteId);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            response.setKeynotes(keynotes);

        } catch (KeynoteServiceUnavailableException e) {
            // Circuit is OPEN — keynote-service is down
            // Don't return empty array silently. Give the user context.
            log.warn("keynote-service unavailable — returning conference without keynote details");

            // Option A: Return degraded placeholder keynotes (better UX)
            List<KeynoteResponse> degradedKeynotes = conference.getKeynoteIds().stream()
                    .map(keynoteId -> {
                        KeynoteResponse placeholder = new KeynoteResponse();
                        placeholder.setId(keynoteId);
                        placeholder.setNom("Temporarily Unavailable");
                        placeholder.setPrenom("");
                        placeholder.setEmail("N/A");
                        placeholder.setFonction("N/A");
                        return placeholder;
                    })
                    .collect(Collectors.toList());

            response.setKeynotes(degradedKeynotes);
        }

        return response;
    }

    @Override
    public Page<ConferenceResponse> searchConferences(String title, ConferenceType type, Pageable pageable) {
        Page<Conference> conferences;

        if (title != null && !title.isEmpty()) {
            conferences = conferenceRepository.findByTitleContainingIgnoreCase(title, pageable);
        } else if (type != null) {
            conferences = conferenceRepository.findByType(type, pageable);
        } else {
            conferences = conferenceRepository.findAll(pageable);
        }

        return conferences.map(mapper::toResponse);
    }

    @Transactional
    @Override
    public void updateScore(Long conferenceId) {
        Conference conference = conferenceRepository.findById(conferenceId).orElseThrow();
        double avg = conference.getReviews()
                .stream()
                .mapToInt(Review::getStars)
                .average()
                .orElse(0);
        conference.setScore(avg);
        conferenceRepository.save(conference);
    }

    @Override
    public List<ConferenceResponse> getAllConferences() {
        return conferenceRepository.findAll().stream()
                .map(conference -> {
                    ConferenceResponse response = mapper.toResponse(conference);

                    if (conference.getKeynoteIds() != null && !conference.getKeynoteIds().isEmpty()) {
                        List<KeynoteResponse> keynotes = conference.getKeynoteIds().stream()
                                .map(keynoteId -> {
                                    try {
                                        return keynoteClient.getKeynoteById(keynoteId);
                                    } catch (FeignException e) {
                                        log.warn("Failed to fetch keynote with id {}", keynoteId, e);
                                        return null;
                                    }
                                })
                                .filter(java.util.Objects::nonNull)
                                .collect(Collectors.toList());
                        response.setKeynotes(keynotes);
                    }

                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ConferenceResponse updateStatus(Long id, ConferenceStatus status) {
        var conference = conferenceRepository.findById(id)
                .orElseThrow(() -> new ConferenceNotFoundException("Conference not found with id: " + id));

        conference.setStatus(status);
        Conference saved = conferenceRepository.save(conference);

        if (status == ConferenceStatus.IN_PROGRESS || status == ConferenceStatus.CANCELLED || status == ConferenceStatus.COMPLETED) {
            List<String> emails = saved.getInscriptions().stream()
                    .map(Inscription::getParticipantEmail)
                    .collect(Collectors.toList());

            String statusLabel = switch (status) {
                case IN_PROGRESS -> "EN_COURS";
                case CANCELLED -> "ANNULE";
                case COMPLETED -> "TERMINE";
                default -> status.name();
            };

            log.debug("Emails: {}", emails);
            ConferenceStatusChangedEvent event = new ConferenceStatusChangedEvent(
                    "CONFERENCE_STATUS_CHANGED",
                    LocalDateTime.now(),
                    saved.getId(),
                    saved.getTitle(),
                    statusLabel,
                    emails
            );

            log.info("Publishing status changed event for conference {}: status={}", saved.getId(), statusLabel);
            eventProducer.publishConferenceStatusChanged(event);
        }

        return mapper.toResponse(saved);
    }
}