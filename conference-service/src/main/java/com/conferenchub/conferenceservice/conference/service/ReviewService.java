package com.conferenchub.conferenceservice.conference.service;

import com.conferenchub.conferenceservice.conference.entity.Conference;
import com.conferenchub.conferenceservice.conference.entity.Review;
import com.conferenchub.conferenceservice.conference.repository.ConferenceRepository;
import com.conferenchub.conferenceservice.conference.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ConferenceRepository conferenceRepository;
    private final ConferenceService conferenceService;

    public void addReview(Long conferenceId, Review review){

        Conference conference = conferenceRepository.findById(conferenceId)
                .orElseThrow();

        review.setConference(conference);

        reviewRepository.save(review);

        conferenceService.updateScore(conferenceId);
    }
}