package com.conferenchub.conferenceservice.conference.dto.response;

import com.conferenchub.conferenceservice.conference.entity.InscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InscriptionResponse {
    private Long id;
    private String participantEmail;
    private String participantName;
    private LocalDate dateInscription;
    private InscriptionStatus status;
    private Long conferenceId;
}
