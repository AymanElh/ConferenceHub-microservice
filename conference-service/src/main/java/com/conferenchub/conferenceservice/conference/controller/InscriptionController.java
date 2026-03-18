package com.conferenchub.conferenceservice.conference.controller;

import com.conferenchub.conferenceservice.conference.service.InscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inscriptions")
@RequiredArgsConstructor
public class InscriptionController {

    private final InscriptionService inscriptionService;

    @PostMapping("/conference/{conferenceId}")
    public ResponseEntity<String> registerToConference(
            @PathVariable Long conferenceId,
            @RequestParam String email,
            @RequestParam String name) {

        inscriptionService.register(conferenceId, email, name);


        return ResponseEntity.ok("Registration successful for " + email);
    }
}