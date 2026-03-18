package com.conferenchub.conferenceservice.conference.service;

import com.conferenchub.conferenceservice.conference.client.KeynoteClient;
import com.conferenchub.conferenceservice.conference.dto.request.CreateConferenceRequest;
import com.conferenchub.conferenceservice.conference.dto.response.ConferenceResponse;
import com.conferenchub.conferenceservice.conference.entity.Conference;
import com.conferenchub.conferenceservice.conference.entity.ConferenceStatus;
import com.conferenchub.conferenceservice.conference.entity.Review;
import com.conferenchub.conferenceservice.conference.exception.KeynoteNotFoundException;
import com.conferenchub.conferenceservice.conference.kafka.ConferenceCreatedEvent;
import com.conferenchub.conferenceservice.conference.kafka.ConferenceEventProducer;
import com.conferenchub.conferenceservice.conference.mapper.ConferenceMapper;
import com.conferenchub.conferenceservice.conference.repository.ConferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConferenceService {

    private final ConferenceRepository conferenceRepository;
    private final ConferenceMapper mapper;
    private final ConferenceEventProducer eventProducer;
    private final KeynoteClient keynoteClient;

    public ConferenceResponse createConference(CreateConferenceRequest request){

        if (request.getKeynoteIds() != null && !request.getKeynoteIds().isEmpty()){
            request.getKeynoteIds().forEach(id -> {
                try {
                    keynoteClient.getKeynoteById(id);
                } catch (Exception e) {
                    throw new KeynoteNotFoundException("Keynote with id " + id + " not found");
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

    public void updateScore(Long conferenceId){

        Conference conference = conferenceRepository.findById(conferenceId)
                .orElseThrow();

        double avg = conference.getReviews()
                .stream()
                .mapToInt(Review::getStars)
                .average()
                .orElse(0);

        conference.setScore(avg);

        conferenceRepository.save(conference);
    }

    public List<ConferenceResponse> getAllConferences(){
        return conferenceRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
}
