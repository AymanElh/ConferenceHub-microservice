package com.conferenchub.conferenceservice.conference.service.impl;

import com.conferenchub.conferenceservice.conference.client.KeynoteClient;
import com.conferenchub.conferenceservice.conference.dto.request.CreateConferenceRequest;
import com.conferenchub.conferenceservice.conference.dto.response.ConferenceResponse;
import com.conferenchub.conferenceservice.conference.dto.response.KeynoteResponse;
import com.conferenchub.conferenceservice.conference.entity.Conference;
import com.conferenchub.conferenceservice.conference.entity.ConferenceStatus;
import com.conferenchub.conferenceservice.conference.entity.ConferenceType;
import com.conferenchub.conferenceservice.conference.entity.Inscription;
import com.conferenchub.conferenceservice.conference.entity.Review;
import com.conferenchub.conferenceservice.conference.exception.KeynoteServiceUnavailableException;
import com.conferenchub.conferenceservice.conference.kafka.producer.ConferenceEventProducer;
import com.conferenchub.conferenceservice.conference.mapper.ConferenceMapper;
import com.conferenchub.conferenceservice.conference.repository.ConferenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private ConferenceServiceImpl service;

    @Test
    void createConference_setsDefaults_saves_andPublishesEvent() {
        CreateConferenceRequest request = new CreateConferenceRequest(
                "Conf 1",
                ConferenceType.ACADEMIC,
                LocalDate.of(2030, 1, 1),
                60,
                List.of(10L, 20L)
        );

        Conference mapped = new Conference();
        mapped.setTitle(request.getTitle());
        mapped.setType(request.getType());
        mapped.setDate(request.getDate());
        mapped.setDuration(request.getDuration());
        mapped.setKeynoteIds(request.getKeynoteIds());
        when(mapper.toEntity(request)).thenReturn(mapped);

        Conference saved = new Conference();
        saved.setId(123L);
        saved.setTitle(mapped.getTitle());
        saved.setType(mapped.getType());
        saved.setDate(mapped.getDate());
        saved.setDuration(mapped.getDuration());
        saved.setKeynoteIds(mapped.getKeynoteIds());
        saved.setStatus(ConferenceStatus.PLANNED);
        saved.setRegisteredNumber(0);
        saved.setScore(0.0);

        when(conferenceRepository.save(any(Conference.class))).thenReturn(saved);

        ConferenceResponse response = new ConferenceResponse();
        response.setId(saved.getId());
        response.setTitle(saved.getTitle());
        when(mapper.toResponse(saved)).thenReturn(response);

        ConferenceResponse out = service.createConference(request);

        ArgumentCaptor<Conference> captor = ArgumentCaptor.forClass(Conference.class);
        verify(conferenceRepository).save(captor.capture());
        Conference toSave = captor.getValue();
        assertThat(toSave.getStatus()).isEqualTo(ConferenceStatus.PLANNED);
        assertThat(toSave.getRegisteredNumber()).isEqualTo(0);
        assertThat(toSave.getScore()).isEqualTo(0.0);

        verify(keynoteClient).getKeynoteById(10L);
        verify(keynoteClient).getKeynoteById(20L);
        verify(eventProducer).publishConferenceCreated(any());
        assertThat(out.getId()).isEqualTo(123L);
    }

    @Test
    void createConference_whenKeynoteServiceUnavailable_throwsFriendlyMessage() {
        CreateConferenceRequest request = new CreateConferenceRequest(
                "Conf 1",
                ConferenceType.ACADEMIC,
                LocalDate.of(2030, 1, 1),
                60,
                List.of(10L)
        );

        doThrow(new KeynoteServiceUnavailableException("down")).when(keynoteClient).getKeynoteById(10L);

        assertThatThrownBy(() -> service.createConference(request))
                .isInstanceOf(KeynoteServiceUnavailableException.class)
                .hasMessageContaining("temporarily unavailable");

        verify(conferenceRepository, never()).save(any());
        verify(eventProducer, never()).publishConferenceCreated(any());
    }

    @Test
    void getConferenceById_whenNoKeynotes_setsEmptyList() {
        Conference conference = new Conference();
        conference.setId(1L);
        conference.setKeynoteIds(null);
        when(conferenceRepository.findById(1L)).thenReturn(Optional.of(conference));

        ConferenceResponse mapped = new ConferenceResponse();
        mapped.setId(1L);
        when(mapper.toResponse(conference)).thenReturn(mapped);

        ConferenceResponse out = service.getConferenceById(1L);

        assertThat(out.getKeynotes()).isEmpty();
        verify(keynoteClient, never()).getKeynoteById(any());
    }

    @Test
    void getConferenceById_whenKeynoteServiceUnavailable_returnsPlaceholders() {
        Conference conference = new Conference();
        conference.setId(1L);
        conference.setKeynoteIds(List.of(10L, 20L));
        when(conferenceRepository.findById(1L)).thenReturn(Optional.of(conference));

        ConferenceResponse mapped = new ConferenceResponse();
        mapped.setId(1L);
        when(mapper.toResponse(conference)).thenReturn(mapped);

        doThrow(new KeynoteServiceUnavailableException("down")).when(keynoteClient).getKeynoteById(10L);

        ConferenceResponse out = service.getConferenceById(1L);

        assertThat(out.getKeynotes()).hasSize(2);
        assertThat(out.getKeynotes().stream().map(KeynoteResponse::getId)).containsExactlyInAnyOrder(10L, 20L);
        assertThat(out.getKeynotes().get(0).getNom()).isEqualTo("Service Unavailable");
    }

    @Test
    void searchConferences_whenTitleProvided_usesTitleQuery() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(conferenceRepository.findByTitleContainingIgnoreCase(eq("java"), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of()));

        Page<ConferenceResponse> out = service.searchConferences("java", null, pageable);

        assertThat(out.getTotalElements()).isZero();
        verify(conferenceRepository).findByTitleContainingIgnoreCase("java", pageable);
        verify(conferenceRepository, never()).findByType(any(), any());
    }

    @Test
    void updateScore_setsAverageStars_andSaves() {
        Review r1 = new Review();
        r1.setStars(5);
        Review r2 = new Review();
        r2.setStars(1);

        Conference conference = new Conference();
        conference.setId(9L);
        conference.setReviews(List.of(r1, r2));

        when(conferenceRepository.findById(9L)).thenReturn(Optional.of(conference));
        when(conferenceRepository.save(any(Conference.class))).thenAnswer(inv -> inv.getArgument(0));

        service.updateScore(9L);

        assertThat(conference.getScore()).isEqualTo(3.0);
        verify(conferenceRepository).save(conference);
    }

    @Test
    void updateStatus_whenInProgress_publishesStatusChangedEvent() {
        Inscription i1 = new Inscription();
        i1.setParticipantEmail("a@example.com");
        Inscription i2 = new Inscription();
        i2.setParticipantEmail("b@example.com");

        Conference conference = new Conference();
        conference.setId(7L);
        conference.setTitle("T");
        conference.setInscriptions(List.of(i1, i2));

        when(conferenceRepository.findById(7L)).thenReturn(Optional.of(conference));
        when(conferenceRepository.save(any(Conference.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Conference.class))).thenReturn(new ConferenceResponse());

        service.updateStatus(7L, ConferenceStatus.IN_PROGRESS);

        verify(eventProducer).publishConferenceStatusChanged(any());
        assertThat(conference.getStatus()).isEqualTo(ConferenceStatus.IN_PROGRESS);
    }
}
