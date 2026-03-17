package com.conferenchub.conferenceservice.conference.repository;

import com.conferenchub.conferenceservice.conference.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByConferenceId(Long conferenceId);
}
