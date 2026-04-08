package com.conferenchub.conferenceservice.conference.client;

import com.conferenchub.conferenceservice.conference.dto.response.KeynoteResponse;
import com.conferenchub.conferenceservice.conference.exception.KeynoteServiceUnavailableException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class KeynoteClientFallback implements FallbackFactory<KeynoteClient> {

    @Override
    public KeynoteClient create(Throwable cause) {
        return new KeynoteClient() {
            @Override
            public KeynoteResponse getKeynoteById(Long id) {
                if (cause instanceof FeignException.NotFound || (cause instanceof FeignException && ((FeignException) cause).status() == 404)) {
                    // Let the 404 propagate to be handled by ConferenceServiceImpl
                    if (cause instanceof FeignException.NotFound) {
                        throw (FeignException.NotFound) cause;
                    }
                    throw (FeignException) cause;
                }

                log.warn("Circuit breaker OPEN or service unavailable. Fallback invoked for getKeynoteById(id={}). Cause: {}", id, cause.getMessage());
                throw new KeynoteServiceUnavailableException(
                        "keynote-service is currently unavailable (circuit open). Cannot verify keynote id=" + id);
            }

            @Override
            public List<KeynoteResponse> getKeynotesByIds(List<Long> ids) {
                log.warn("Circuit breaker OPEN or service unavailable. Fallback invoked for getKeynotesByIds. Cause: {}", cause.getMessage());
                throw new KeynoteServiceUnavailableException(
                        "keynote-service is currently unavailable (circuit open). Cannot fetch keynote details.");
            }
        };
    }
}


