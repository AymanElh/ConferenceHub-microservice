package com.conferenchub.conferenceservice.conference.service.impl;

import com.conferenchub.conferenceservice.conference.client.KeynoteClient;
import com.conferenchub.conferenceservice.conference.dto.request.CreateReviewDto;
import com.conferenchub.conferenceservice.conference.dto.response.KeynoteResponse;
import com.conferenchub.conferenceservice.conference.entity.Conference;
import com.conferenchub.conferenceservice.conference.entity.Review;
import com.conferenchub.conferenceservice.conference.exception.ConferenceNotFoundException;
import com.conferenchub.conferenceservice.conference.kafka.event.NewReviewEvent;
import com.conferenchub.conferenceservice.conference.kafka.producer.ReviewEventProducer;
import com.conferenchub.conferenceservice.conference.mapper.ReviewMapper;
import com.conferenchub.conferenceservice.conference.repository.ConferenceRepository;
import com.conferenchub.conferenceservice.conference.repository.ReviewRepository;
import com.conferenchub.conferenceservice.conference.service.ConferenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ConferenceRepository conferenceRepository;

    @Mock
    private ConferenceService conferenceService;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private ReviewEventProducer reviewEventProducer;

    @Mock
    private KeynoteClient keynoteClient;

    @InjectMocks
    private ReviewServiceImpl service;

    @Test
    void addReview_whenConferenceHasKeynotes_savesReviewUpdatesScoreAndPublishesEvent() {
        CreateReviewDto dto = CreateReviewDto.builder()
                .text("Great session")
                .stars(5)
                .authorEmail("author@example.com")
                .build();

        Conference conference = new Conference();
        conference.setId(10L);
        conference.setKeynoteIds(List.of(1L, 2L));
        when(conferenceRepository.findById(10L)).thenReturn(Optional.of(conference));

        KeynoteResponse keynote1 = new KeynoteResponse();
        keynote1.setEmail("speaker1@example.com");
        KeynoteResponse keynote2 = new KeynoteResponse();
        keynote2.setEmail("speaker2@example.com");
        when(keynoteClient.getKeynotesByIds(List.of(1L, 2L))).thenReturn(List.of(keynote1, keynote2));

        Review mapped = new Review();
        mapped.setText(dto.getText());
        mapped.setStars(dto.getStars());
        mapped.setAuthorEmail(dto.getAuthorEmail());
        when(reviewMapper.toEntity(dto)).thenReturn(mapped);

        when(reviewRepository.save(mapped)).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(77L);
            return review;
        });

        service.addReview(10L, dto);

        assertThat(mapped.getConference()).isSameAs(conference);
        verify(reviewRepository).save(mapped);
        verify(conferenceService).updateScore(10L);

        ArgumentCaptor<NewReviewEvent> eventCaptor = ArgumentCaptor.forClass(NewReviewEvent.class);
        verify(reviewEventProducer).publishReviewCreated(eventCaptor.capture());
        NewReviewEvent event = eventCaptor.getValue();
        assertThat(event.eventType()).isEqualTo("NEW_REVIEW");
        assertThat(event.reviewId()).isEqualTo(77L);
        assertThat(event.conferenceId()).isEqualTo(10L);
        assertThat(event.keynoteEmails()).containsExactly("speaker1@example.com", "speaker2@example.com");
    }

    @Test
    void addReview_whenConferenceHasNoKeynotes_publishesEventWithEmptyKeynoteEmails() {
        CreateReviewDto dto = CreateReviewDto.builder()
                .text("Useful")
                .stars(4)
                .authorEmail("author@example.com")
                .build();

        Conference conference = new Conference();
        conference.setId(11L);
        conference.setKeynoteIds(List.of());
        when(conferenceRepository.findById(11L)).thenReturn(Optional.of(conference));

        Review mapped = new Review();
        mapped.setText(dto.getText());
        mapped.setStars(dto.getStars());
        mapped.setAuthorEmail(dto.getAuthorEmail());
        when(reviewMapper.toEntity(dto)).thenReturn(mapped);
        when(reviewRepository.save(mapped)).thenReturn(mapped);

        service.addReview(11L, dto);

        verify(keynoteClient, never()).getKeynotesByIds(any());
        ArgumentCaptor<NewReviewEvent> eventCaptor = ArgumentCaptor.forClass(NewReviewEvent.class);
        verify(reviewEventProducer).publishReviewCreated(eventCaptor.capture());
        assertThat(eventCaptor.getValue().keynoteEmails()).isEmpty();
    }

    @Test
    void addReview_whenConferenceMissing_throwsAndDoesNotSave() {
        CreateReviewDto dto = CreateReviewDto.builder()
                .text("Missing")
                .stars(3)
                .authorEmail("author@example.com")
                .build();
        when(conferenceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addReview(99L, dto))
                .isInstanceOf(ConferenceNotFoundException.class)
                .hasMessageContaining("Conference not found with id: 99");

        verify(reviewRepository, never()).save(any());
        verify(reviewEventProducer, never()).publishReviewCreated(any());
        verify(conferenceService, never()).updateScore(any());
    }
}
