package com.conferenchub.conferenceservice.conference.service;

import com.conferenchub.conferenceservice.conference.entity.Conference;
import com.conferenchub.conferenceservice.conference.entity.Inscription;
import com.conferenchub.conferenceservice.conference.entity.InscriptionStatus;
import com.conferenchub.conferenceservice.conference.repository.ConferenceRepository;
import com.conferenchub.conferenceservice.conference.repository.InscriptionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InscriptionService {

    private final InscriptionRepository inscriptionRepository;
    private final ConferenceRepository conferenceRepository;

    @Transactional
    public void register(Long conferenceId, String email, String name){
        if(inscriptionRepository.existsByConferenceIdAndParticipantEmail(conferenceId, email)){
            throw new RuntimeException("Participant already registered for this conference");
        }

        Conference conference = conferenceRepository.findById(conferenceId)
                .orElseThrow(() -> new RuntimeException("Conference not found"));

        Inscription inscription = new Inscription();
        inscription.setConference(conference);
        inscription.setParticipantEmail(email);
        inscription.setParticipantName(name);
        inscription.setStatus(InscriptionStatus.CONFIRMED);

        inscriptionRepository.save(inscription);

        conference.setRegisteredNumber(conference.getRegisteredNumber() + 1);
        conferenceRepository.save(conference);
    }
}