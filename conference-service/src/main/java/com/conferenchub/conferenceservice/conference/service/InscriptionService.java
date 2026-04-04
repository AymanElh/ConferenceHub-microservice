package com.conferenchub.conferenceservice.conference.service;

public interface InscriptionService {
    void register(Long conferenceId, String email, String name);
}

