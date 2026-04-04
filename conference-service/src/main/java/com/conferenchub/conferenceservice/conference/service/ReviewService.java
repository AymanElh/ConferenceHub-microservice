package com.conferenchub.conferenceservice.conference.service;

import com.conferenchub.conferenceservice.conference.entity.Review;

public interface ReviewService {
    void addReview(Long conferenceId, Review review);
}

