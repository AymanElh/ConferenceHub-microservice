package com.conferenchub.conferenceservice.conference.service.impl;

import com.conferenchub.conferenceservice.conference.entity.Conference;
import com.conferenchub.conferenceservice.conference.entity.Review;
import com.conferenchub.conferenceservice.conference.repository.ConferenceRepository;
import com.conferenchub.conferenceservice.conference.repository.ReviewRepository;
import com.conferenchub.conferenceservice.conference.service.ConferenceService;
import com.conferenchub.conferenceservice.conference.service.ReviewService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ConferenceRepository conferenceRepository;
    private final ConferenceService conferenceService;

    @Transactional
    @Override
    public void addReview(Long conferenceId, Review review){
        Conference conference = conferenceRepository.findById(conferenceId)
                .orElseThrow(() -> new RuntimeException("Conference not found"));

        review.setConference(conference);
        reviewRepository.save(review);

        conferenceService.updateScore(conferenceId);
    }
}