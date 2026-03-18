package com.conferenchub.conferenceservice.conference.controller;

import com.conferenchub.conferenceservice.conference.entity.Review;
import com.conferenchub.conferenceservice.conference.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/conference/{conferenceId}")
    public ResponseEntity<Void> addReview(
            @PathVariable Long conferenceId,
            @RequestBody @Valid Review review) {

        reviewService.addReview(conferenceId, review);
        return ResponseEntity.noContent().build();
    }
}