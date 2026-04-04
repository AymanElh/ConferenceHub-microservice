package com.conferenchub.conferenceservice.conference.service;

import com.conferenchub.conferenceservice.conference.dto.request.CreateConferenceRequest;
import com.conferenchub.conferenceservice.conference.dto.response.ConferenceResponse;
import com.conferenchub.conferenceservice.conference.entity.ConferenceStatus;
import com.conferenchub.conferenceservice.conference.entity.ConferenceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ConferenceService {
    ConferenceResponse createConference(CreateConferenceRequest request);
    ConferenceResponse getConferenceById(Long id);
    Page<ConferenceResponse> searchConferences(String title, ConferenceType type, Pageable pageable);
    void updateScore(Long conferenceId);
    List<ConferenceResponse> getAllConferences();
    ConferenceResponse updateStatus(Long id, ConferenceStatus status);
}
