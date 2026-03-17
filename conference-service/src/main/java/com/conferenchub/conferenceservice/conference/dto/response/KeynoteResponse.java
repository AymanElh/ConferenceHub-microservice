package com.conferenchub.conferenceservice.conference.dto.response;

import lombok.Data;

@Data
public class KeynoteResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String fonction;
}
