package com.conferenchub.conferenceservice.conference.service.impl;

import com.conferenchub.conferenceservice.conference.dto.request.CreateInscriptionDto;
import com.conferenchub.conferenceservice.conference.dto.response.InscriptionResponse;
import com.conferenchub.conferenceservice.conference.entity.Conference;
import com.conferenchub.conferenceservice.conference.entity.Inscription;
import com.conferenchub.conferenceservice.conference.entity.InscriptionStatus;
import com.conferenchub.conferenceservice.conference.kafka.event.NewInscriptionEvent;
import com.conferenchub.conferenceservice.conference.kafka.producer.NewInscriptionEventProducer;
import com.conferenchub.conferenceservice.conference.mapper.InscriptionMapper;
import com.conferenchub.conferenceservice.conference.repository.ConferenceRepository;
import com.conferenchub.conferenceservice.conference.repository.InscriptionRepository;
import com.conferenchub.conferenceservice.conference.service.InscriptionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InscriptionServiceImpl implements InscriptionService {

    private final InscriptionRepository inscriptionRepository;
    private final ConferenceRepository conferenceRepository;
    private final NewInscriptionEventProducer newInscriptionEventProducer;
    private final InscriptionMapper inscriptionMapper;

    @Transactional
    @Override
    public void register(Long conferenceId, CreateInscriptionDto inscriptionDto){
        String email = inscriptionDto.getParticipantEmail();
        String name = inscriptionDto.getParticipantName();

        if(inscriptionRepository.existsByConferenceIdAndParticipantEmail(conferenceId, email)){
            throw new RuntimeException("Participant already registered for this conference");
        }

        Conference conference = conferenceRepository.findById(conferenceId)
                .orElseThrow(() -> new RuntimeException("Conference not found"));

        Inscription inscription = inscriptionMapper.toEntity(inscriptionDto);
        inscription.setConference(conference);
        inscription.setStatus(InscriptionStatus.CONFIRMED);

        Inscription saved = inscriptionRepository.save(inscription);

        NewInscriptionEvent inscriptionEvent = new NewInscriptionEvent(
                "NEW_INSCRIPTION",
                java.time.LocalDateTime.now(),
                saved.getId(),
                conferenceId,
                email,
                name
        );
         newInscriptionEventProducer.publishNewInscription(inscriptionEvent);

        conference.setRegisteredNumber(conference.getRegisteredNumber() + 1);
        conferenceRepository.save(conference);
    }

    @Override
    public List<InscriptionResponse> getByConferenceId(Long conferenceId) {
        if (!conferenceRepository.existsById(conferenceId)) {
            throw new RuntimeException("Conference not found with id: " + conferenceId);
        }
        List<Inscription> inscriptions = inscriptionRepository.findByConferenceId(conferenceId);
        return inscriptionMapper.toResponseList(inscriptions);
    }
}