package com.conferenchub.conferenceservice.conference.service.impl;

import com.conferenchub.conferenceservice.conference.dto.request.CreateInscriptionDto;
import com.conferenchub.conferenceservice.conference.dto.response.InscriptionResponse;
import com.conferenchub.conferenceservice.conference.entity.Conference;
import com.conferenchub.conferenceservice.conference.entity.Inscription;
import com.conferenchub.conferenceservice.conference.entity.InscriptionStatus;
import com.conferenchub.conferenceservice.conference.exception.ConferenceNotFoundException;
import com.conferenchub.conferenceservice.conference.kafka.event.NewInscriptionEvent;
import com.conferenchub.conferenceservice.conference.kafka.producer.NewInscriptionEventProducer;
import com.conferenchub.conferenceservice.conference.mapper.InscriptionMapper;
import com.conferenchub.conferenceservice.conference.repository.ConferenceRepository;
import com.conferenchub.conferenceservice.conference.repository.InscriptionRepository;
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
class InscriptionServiceImplTest {

    @Mock
    private InscriptionRepository inscriptionRepository;

    @Mock
    private ConferenceRepository conferenceRepository;

    @Mock
    private NewInscriptionEventProducer newInscriptionEventProducer;

    @Mock
    private InscriptionMapper inscriptionMapper;

    @InjectMocks
    private InscriptionServiceImpl service;

    @Test
    void register_whenParticipantIsNew_savesConfirmedInscriptionPublishesEventAndIncrementsConference() {
        CreateInscriptionDto dto = CreateInscriptionDto.builder()
                .participantEmail("participant@example.com")
                .participantName("Jane Doe")
                .build();

        Conference conference = new Conference();
        conference.setId(20L);
        conference.setRegisteredNumber(2);

        when(inscriptionRepository.existsByConferenceIdAndParticipantEmail(20L, "participant@example.com"))
                .thenReturn(false);
        when(conferenceRepository.findById(20L)).thenReturn(Optional.of(conference));

        Inscription mapped = new Inscription();
        mapped.setParticipantEmail(dto.getParticipantEmail());
        mapped.setParticipantName(dto.getParticipantName());
        when(inscriptionMapper.toEntity(dto)).thenReturn(mapped);
        when(inscriptionRepository.save(mapped)).thenAnswer(invocation -> {
            Inscription inscription = invocation.getArgument(0);
            inscription.setId(55L);
            return inscription;
        });

        service.register(20L, dto);

        assertThat(mapped.getConference()).isSameAs(conference);
        assertThat(mapped.getStatus()).isEqualTo(InscriptionStatus.CONFIRMED);
        assertThat(conference.getRegisteredNumber()).isEqualTo(3);
        verify(conferenceRepository).save(conference);

        ArgumentCaptor<NewInscriptionEvent> eventCaptor = ArgumentCaptor.forClass(NewInscriptionEvent.class);
        verify(newInscriptionEventProducer).publishNewInscription(eventCaptor.capture());
        NewInscriptionEvent event = eventCaptor.getValue();
        assertThat(event.eventType()).isEqualTo("NEW_INSCRIPTION");
        assertThat(event.inscriptionId()).isEqualTo(55L);
        assertThat(event.conferenceId()).isEqualTo(20L);
        assertThat(event.participantEmail()).isEqualTo("participant@example.com");
        assertThat(event.participantNom()).isEqualTo("Jane Doe");
    }

    @Test
    void register_whenParticipantAlreadyRegistered_throwsAndDoesNotLoadConference() {
        CreateInscriptionDto dto = CreateInscriptionDto.builder()
                .participantEmail("participant@example.com")
                .participantName("Jane Doe")
                .build();
        when(inscriptionRepository.existsByConferenceIdAndParticipantEmail(20L, "participant@example.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> service.register(20L, dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Participant already registered");

        verify(conferenceRepository, never()).findById(any());
        verify(inscriptionRepository, never()).save(any());
        verify(newInscriptionEventProducer, never()).publishNewInscription(any());
    }

    @Test
    void register_whenConferenceMissing_throwsAndDoesNotSave() {
        CreateInscriptionDto dto = CreateInscriptionDto.builder()
                .participantEmail("participant@example.com")
                .participantName("Jane Doe")
                .build();
        when(inscriptionRepository.existsByConferenceIdAndParticipantEmail(99L, "participant@example.com"))
                .thenReturn(false);
        when(conferenceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.register(99L, dto))
                .isInstanceOf(ConferenceNotFoundException.class)
                .hasMessageContaining("Conference not found with id: 99");

        verify(inscriptionRepository, never()).save(any());
        verify(newInscriptionEventProducer, never()).publishNewInscription(any());
    }

    @Test
    void getByConferenceId_whenConferenceExists_returnsMappedInscriptions() {
        Inscription inscription = new Inscription();
        inscription.setId(1L);
        List<Inscription> inscriptions = List.of(inscription);
        List<InscriptionResponse> responses = List.of(new InscriptionResponse(
                1L,
                "participant@example.com",
                "Jane Doe",
                null,
                InscriptionStatus.CONFIRMED,
                20L
        ));

        when(conferenceRepository.existsById(20L)).thenReturn(true);
        when(inscriptionRepository.findByConferenceId(20L)).thenReturn(inscriptions);
        when(inscriptionMapper.toResponseList(inscriptions)).thenReturn(responses);

        List<InscriptionResponse> out = service.getByConferenceId(20L);

        assertThat(out).isSameAs(responses);
        verify(inscriptionRepository).findByConferenceId(20L);
    }

    @Test
    void getByConferenceId_whenConferenceMissing_throwsAndDoesNotQueryInscriptions() {
        when(conferenceRepository.existsById(404L)).thenReturn(false);

        assertThatThrownBy(() -> service.getByConferenceId(404L))
                .isInstanceOf(ConferenceNotFoundException.class)
                .hasMessageContaining("Conference not found with id: 404");

        verify(inscriptionRepository, never()).findByConferenceId(any());
    }
}
