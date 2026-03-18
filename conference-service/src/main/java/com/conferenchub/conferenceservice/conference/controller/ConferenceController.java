package com.conferenchub.conferenceservice.conference.controller;

import com.conferenchub.conferenceservice.conference.dto.request.CreateConferenceRequest;
import com.conferenchub.conferenceservice.conference.dto.request.UpdateConferenceRequest;
import com.conferenchub.conferenceservice.conference.dto.response.ConferenceResponse;
import com.conferenchub.conferenceservice.conference.service.ConferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conferences")
@RequiredArgsConstructor
public class ConferenceController {

    private final ConferenceService conferenceService;

    @PostMapping
    public ResponseEntity<ConferenceResponse> createConference(
            @RequestBody @Valid CreateConferenceRequest request){

        return ResponseEntity.ok(
                conferenceService.createConference(request)
        );
    }

    @GetMapping
    public ResponseEntity<List<ConferenceResponse>> getAllConferences() {
        return ResponseEntity.ok(conferenceService.getAllConferences());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ConferenceResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateConferenceRequest updateConferenceRequest) {
        return ResponseEntity.ok(conferenceService.updateStatus(id, updateConferenceRequest.getStatus()));
    }

}
