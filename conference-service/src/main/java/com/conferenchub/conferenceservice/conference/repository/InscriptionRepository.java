package com.conferenchub.conferenceservice.conference.repository;

import com.conferenchub.conferenceservice.conference.entity.Inscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InscriptionRepository extends JpaRepository<Inscription, Long> {

    boolean existsByConferenceIdAndParticipantEmail(Long conferenceId,String email);

    List<Inscription> findByConferenceId(Long conferenceId);
}