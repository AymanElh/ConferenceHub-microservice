package org.example.keynoteservice.exception;

public class KeynoteNotFoundException extends RuntimeException {
    public KeynoteNotFoundException(String message) {
        super(message);
    }
}
