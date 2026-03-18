package com.conferenchub.conferenceservice.conference.controller;

import com.conferenchub.conferenceservice.conference.dto.request.CreateConferenceRequest;
import com.conferenchub.conferenceservice.conference.dto.request.UpdateConferenceRequest;
import com.conferenchub.conferenceservice.conference.dto.response.ConferenceResponse;
import com.conferenchub.conferenceservice.conference.entity.ConferenceType;
import com.conferenchub.conferenceservice.conference.service.ConferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/conferences")
@RequiredArgsConstructor
public class ConferenceController {

    private final ConferenceService conferenceService;

    @PostMapping
    public ResponseEntity<ConferenceResponse> createConference(
            @RequestBody @Valid CreateConferenceRequest request) {
        return new ResponseEntity<>(conferenceService.createConference(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConferenceResponse> getConference(@PathVariable Long id) {
        return ResponseEntity.ok(conferenceService.getConferenceById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ConferenceResponse>> searchConferences(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) ConferenceType type,
            Pageable pageable) {
        return ResponseEntity.ok(conferenceService.searchConferences(title, type, pageable));
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
