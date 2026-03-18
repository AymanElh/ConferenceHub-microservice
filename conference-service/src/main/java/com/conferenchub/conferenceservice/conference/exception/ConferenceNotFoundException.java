package com.conferenchub.conferenceservice.conference.exception;

public class ConferenceNotFoundException extends RuntimeException {
    public ConferenceNotFoundException(String message) {
        super(message);
    }
}

