package com.conferenchub.conferenceservice.conference.controller;

import com.conferenchub.conferenceservice.conference.dto.request.CreateInscriptionDto;
import com.conferenchub.conferenceservice.conference.dto.response.InscriptionResponse;
import com.conferenchub.conferenceservice.conference.service.InscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inscriptions")
@RequiredArgsConstructor
public class InscriptionController {

    private final InscriptionService inscriptionService;

    @PostMapping("/conference/{conferenceId}")
    public ResponseEntity<String> registerToConference(
            @PathVariable Long conferenceId,
            @RequestBody @Valid CreateInscriptionDto inscriptionDto) {

        inscriptionService.register(conferenceId, inscriptionDto);

        return ResponseEntity.ok("Registration successful for " + inscriptionDto.getParticipantEmail());
    }

    @GetMapping("/conference/{conferenceId}")
    public ResponseEntity<List<InscriptionResponse>> getInscriptionsByConference(@PathVariable Long conferenceId) {
        return ResponseEntity.ok(inscriptionService.getByConferenceId(conferenceId));
    }
}