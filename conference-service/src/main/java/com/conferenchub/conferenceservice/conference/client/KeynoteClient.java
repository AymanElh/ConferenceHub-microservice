package com.conferenchub.conferenceservice.conference.client;

import com.conferenchub.conferenceservice.conference.dto.response.KeynoteResponse;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "keynote-service", path = "/api")
public interface KeynoteClient {

    @GetMapping("/keynotes/{id}")
    KeynoteResponse getKeynoteById(@PathVariable Long id);

    @GetMapping("/keynotes/batch")
    List<KeynoteResponse> getKeynotesByIds(@RequestParam("ids") List<Long> ids);
}