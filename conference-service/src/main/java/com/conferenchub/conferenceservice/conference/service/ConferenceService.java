package com.conferenchub.conferenceservice.conference.service;

import com.conferenchub.conferenceservice.conference.dto.request.CreateConferenceRequest;
import com.conferenchub.conferenceservice.conference.dto.response.ConferenceResponse;
import com.conferenchub.conferenceservice.conference.entity.Conference;
import com.conferenchub.conferenceservice.conference.entity.ConferenceStatus;
import com.conferenchub.conferenceservice.conference.entity.Review;
import com.conferenchub.conferenceservice.conference.kafka.ConferenceEventProducer;
import com.conferenchub.conferenceservice.conference.mapper.ConferenceMapper;
import com.conferenchub.conferenceservice.conference.repository.ConferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConferenceService {

    private final ConferenceRepository conferenceRepository;
    private final ConferenceMapper mapper;
    private final ConferenceEventProducer eventProducer;

    public ConferenceResponse createConference(CreateConferenceRequest request){

        Conference conference = mapper.toEntity(request);

        conference.setStatus(ConferenceStatus.PLANNED);
        conference.setRegisteredNumber(0);
        conference.setScore(0.0);

        Conference saved = conferenceRepository.save(conference);

//        eventProducer.publishConferenceCreated(saved);

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
}
