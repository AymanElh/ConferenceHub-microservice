package com.conferenchub.conferenceservice.conference.service;

import com.conferenchub.conferenceservice.conference.dto.request.CreateInscriptionDto;

public interface InscriptionService {
    void register(Long conferenceId, CreateInscriptionDto inscriptionDto);
}

