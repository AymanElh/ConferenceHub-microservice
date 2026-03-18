package com.conferenchub.conferenceservice.conference.service;

import com.conferenchub.conferenceservice.conference.dto.request.CreateConferenceRequest;
import com.conferenchub.conferenceservice.conference.dto.response.ConferenceResponse;
import com.conferenchub.conferenceservice.conference.entity.Conference;
import com.conferenchub.conferenceservice.conference.entity.ConferenceStatus;
import com.conferenchub.conferenceservice.conference.entity.ConferenceType;
import com.conferenchub.conferenceservice.conference.entity.Review;
import com.conferenchub.conferenceservice.conference.mapper.ConferenceMapper;
import com.conferenchub.conferenceservice.conference.repository.ConferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConferenceService {

    private final ConferenceRepository conferenceRepository;
    private final ConferenceMapper mapper;

    public ConferenceResponse createConference(CreateConferenceRequest request){
        Conference conference = mapper.toEntity(request);
        conference.setStatus(ConferenceStatus.PLANNED);
        conference.setRegisteredNumber(0);
        conference.setScore(0.0);
        return mapper.toResponse(conferenceRepository.save(conference));
    }

    public ConferenceResponse getConferenceById(Long id) {
        Conference conference = conferenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conference not found with id: " + id));
        return mapper.toResponse(conference);
    }

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
    public void updateScore(Long conferenceId){
        Conference conference = conferenceRepository.findById(conferenceId).orElseThrow();
        double avg = conference.getReviews()
                .stream()
                .mapToInt(Review::getStars)
                .average()
                .orElse(0);
        conference.setScore(avg);
        conferenceRepository.save(conference);
    }
}