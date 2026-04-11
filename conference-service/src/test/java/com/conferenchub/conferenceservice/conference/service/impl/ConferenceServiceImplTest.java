package com.conferenchub.conferenceservice.conference.service.impl;

import com.conferenchub.conferenceservice.conference.client.KeynoteClient;
import com.conferenchub.conferenceservice.conference.dto.request.CreateConferenceRequest;
import com.conferenchub.conferenceservice.conference.dto.response.ConferenceResponse;
import com.conferenchub.conferenceservice.conference.dto.response.KeynoteResponse;
import com.conferenchub.conferenceservice.conference.entity.Conference;
import com.conferenchub.conferenceservice.conference.entity.ConferenceStatus;
import com.conferenchub.conferenceservice.conference.entity.ConferenceType;
import com.conferenchub.conferenceservice.conference.exception.KeynoteServiceUnavailableException;
import com.conferenchub.conferenceservice.conference.kafka.event.ConferenceCreatedEvent;
import com.conferenchub.conferenceservice.conference.kafka.event.ConferenceStatusChangedEvent;
import com.conferenchub.conferenceservice.conference.kafka.producer.ConferenceEventProducer;
import com.conferenchub.conferenceservice.conference.mapper.ConferenceMapper;
import com.conferenchub.conferenceservice.conference.repository.ConferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConferenceServiceImplTest {

    @Mock
    private ConferenceRepository conferenceRepository;

    @Mock
    private ConferenceMapper mapper;

    @Mock
    private ConferenceEventProducer eventProducer;

    @Mock
    private KeynoteClient keynoteClient;

    @InjectMocks
    private ConferenceServiceImpl conferenceService;

    private Conference conference;
    private ConferenceResponse conferenceResponse;
    private CreateConferenceRequest createRequest;

    @BeforeEach
    void setUp() {
        conference = new Conference();
        conference.setId(1L);
        conference.setTitle("AI Summit");
        conference.setType(ConferenceType.ACADEMIC);
        conference.setStatus(ConferenceStatus.PLANNED);
        conference.setKeynoteIds(List.of(100L));

        conferenceResponse = new ConferenceResponse();
        conferenceResponse.setId(1L);
        conferenceResponse.setTitle("AI Summit");

        createRequest = new CreateConferenceRequest();
        createRequest.setTitle("AI Summit");
        createRequest.setKeynoteIds(List.of(100L));
    }

    @Test
    void createConference_ShouldSaveAndPublishEvent_WhenKeynotesExist() {
        // Given
        when(keynoteClient.getKeynoteById(100L)).thenReturn(new KeynoteResponse());
        when(mapper.toEntity(createRequest)).thenReturn(conference);
        when(conferenceRepository.save(any(Conference.class))).thenReturn(conference);
        when(mapper.toResponse(conference)).thenReturn(conferenceResponse);

        // When
        ConferenceResponse result = conferenceService.createConference(createRequest);

        // Then
        assertThat(result).isNotNull();
        verify(keynoteClient).getKeynoteById(100L);
        verify(conferenceRepository).save(any(Conference.class));
        verify(eventProducer).publishConferenceCreated(any(ConferenceCreatedEvent.class));
    }

    @Test
    void getConferenceById_ShouldFetchKeynotes_WhenServiceIsAvailable() {
        // Given
        when(conferenceRepository.findById(1L)).thenReturn(Optional.of(conference));
        when(mapper.toResponse(conference)).thenReturn(conferenceResponse);
        KeynoteResponse kn = new KeynoteResponse();
        kn.setId(100L);
        kn.setNom("Smith");
        when(keynoteClient.getKeynoteById(100L)).thenReturn(kn);

        // When
        ConferenceResponse result = conferenceService.getConferenceById(1L);

        // Then
        assertThat(result.getKeynotes()).hasSize(1);
        assertThat(result.getKeynotes().get(0).getNom()).isEqualTo("Smith");
    }

    @Test
    void getConferenceById_ShouldReturnPlaceholders_WhenKeynoteServiceIsUnavailable() {
        // Given
        when(conferenceRepository.findById(1L)).thenReturn(Optional.of(conference));
        when(mapper.toResponse(conference)).thenReturn(conferenceResponse);
        
        // Simulate Resilience4j or Feign Exception triggering our catch block
        when(keynoteClient.getKeynoteById(100L)).thenThrow(new KeynoteServiceUnavailableException("Service Down"));

        // When
        ConferenceResponse result = conferenceService.getConferenceById(1L);

        // Then
        assertThat(result.getKeynotes()).hasSize(1);
        assertThat(result.getKeynotes().get(0).getNom()).isEqualTo("Service Unavailable");
        assertThat(result.getKeynotes().get(0).getPrenom()).isEqualTo("(Resilience)");
    }

    @Test
    void updateStatus_ShouldPublishEvent_WhenStatusChangesToInProgress() {
        // Given
        conference.setInscriptions(Collections.emptyList());
        when(conferenceRepository.findById(1L)).thenReturn(Optional.of(conference));
        when(conferenceRepository.save(any(Conference.class))).thenReturn(conference);
        when(mapper.toResponse(conference)).thenReturn(conferenceResponse);

        // When
        conferenceService.updateStatus(1L, ConferenceStatus.IN_PROGRESS);

        // Then
        verify(eventProducer).publishConferenceStatusChanged(any(ConferenceStatusChangedEvent.class));
        assertThat(conference.getStatus()).isEqualTo(ConferenceStatus.IN_PROGRESS);
    }
}
