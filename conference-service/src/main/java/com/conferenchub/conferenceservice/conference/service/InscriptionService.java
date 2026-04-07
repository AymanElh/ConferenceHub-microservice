package com.conferenchub.conferenceservice.conference.service;

import com.conferenchub.conferenceservice.conference.dto.request.CreateInscriptionDto;
import com.conferenchub.conferenceservice.conference.dto.response.InscriptionResponse;

import java.util.List;

public interface InscriptionService {
    void register(Long conferenceId, CreateInscriptionDto inscriptionDto);

    List<InscriptionResponse> getByConferenceId(Long conferenceId);
}

