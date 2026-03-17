package com.conferenchub.conferenceservice.conference.repository;

import com.conferenchub.conferenceservice.conference.entity.Conference;
import com.conferenchub.conferenceservice.conference.entity.ConferenceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConferenceRepository extends JpaRepository<Conference, Long> {

    Page<Conference> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Conference> findByType(ConferenceType type, Pageable pageable);

}
