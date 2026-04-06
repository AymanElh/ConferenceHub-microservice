package com.conferenchub.conferenceservice.conference.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInscriptionDto {

    @Email
    @NotBlank
    private String participantEmail;

    @NotBlank
    private String participantName;
}
