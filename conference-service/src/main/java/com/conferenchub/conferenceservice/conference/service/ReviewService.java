package com.conferenchub.conferenceservice.conference.service;

import com.conferenchub.conferenceservice.conference.dto.request.CreateReviewDto;

public interface ReviewService {
    void addReview(Long conferenceId, CreateReviewDto reviewDto);
}

