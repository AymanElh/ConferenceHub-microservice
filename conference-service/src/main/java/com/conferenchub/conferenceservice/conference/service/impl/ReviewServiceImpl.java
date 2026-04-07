package com.conferenchub.conferenceservice.conference.service.impl;

import com.conferenchub.conferenceservice.conference.client.KeynoteClient;
import com.conferenchub.conferenceservice.conference.dto.request.CreateReviewDto;
import com.conferenchub.conferenceservice.conference.entity.Conference;
import com.conferenchub.conferenceservice.conference.entity.Review;
import com.conferenchub.conferenceservice.conference.kafka.event.NewReviewEvent;
import com.conferenchub.conferenceservice.conference.kafka.producer.ReviewEventProducer;
import com.conferenchub.conferenceservice.conference.mapper.ReviewMapper;
import com.conferenchub.conferenceservice.conference.repository.ConferenceRepository;
import com.conferenchub.conferenceservice.conference.repository.ReviewRepository;
import com.conferenchub.conferenceservice.conference.service.ConferenceService;
import com.conferenchub.conferenceservice.conference.service.ReviewService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ConferenceRepository conferenceRepository;
    private final ConferenceService conferenceService;
    private final ReviewMapper reviewMapper;
    private final ReviewEventProducer reviewEventProducer;
    private final KeynoteClient keynoteClient;

    @Transactional
    @Override
    public void addReview(Long conferenceId, CreateReviewDto reviewDto){
        Conference conference = conferenceRepository.findById(conferenceId)
                .orElseThrow(() -> new RuntimeException("Conference not found"));

        Review review = reviewMapper.toEntity(reviewDto);
        review.setConference(conference);
        reviewRepository.save(review);

        List<String> keynoteEmails = conference.getKeynoteIds().stream()
                .map(id -> keynoteClient.getKeynoteById(id).getEmail())
                .collect(Collectors.toList());

        NewReviewEvent event = new NewReviewEvent(
            "NEW_REVIEW",
            java.time.LocalDateTime.now(),
            review.getId(),
            conferenceId,
            review.getStars(),
            review.getText(),
            review.getAuthorEmail(),
            keynoteEmails
        );

        reviewEventProducer.publishReviewCreated(event);


        conferenceService.updateScore(conferenceId);
    }
}