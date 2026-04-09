package com.conferenchub.conferenceservice.conference.client;

import com.conferenchub.conferenceservice.conference.dto.response.KeynoteResponse;
import com.conferenchub.conferenceservice.conference.exception.KeynoteNotFoundException;
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
                Throwable rootCause = unwrap(cause);

                if (rootCause instanceof KeynoteNotFoundException || rootCause instanceof FeignException.NotFound || (rootCause instanceof FeignException && ((FeignException) rootCause).status() == 404)) {
                    log.debug("Keynote id={} not found. Propagating 404.", id);
                    throw (RuntimeException) rootCause;
                }

                log.warn("Keynote service unavailable for getKeynoteById(id={}). Cause: {}", id, rootCause.getMessage());
                throw new KeynoteServiceUnavailableException(
                        "keynote-service is currently unavailable (circuit open or system error). Cannot verify keynote id=" + id);
            }

            @Override
            public List<KeynoteResponse> getKeynotesByIds(List<Long> ids) {
                Throwable rootCause = unwrap(cause);
                if (rootCause instanceof KeynoteNotFoundException || rootCause instanceof FeignException.NotFound || (rootCause instanceof FeignException && ((FeignException) rootCause).status() == 404)) {
                    throw (RuntimeException) rootCause;
                }

                log.warn("Keynote service unavailable for getKeynotesByIds. Cause: {}", rootCause.getMessage());
                throw new KeynoteServiceUnavailableException(
                        "keynote-service is currently unavailable (circuit open or system error). Cannot fetch keynote details.");
            }
        };
    }

    private Throwable unwrap(Throwable e) {
        Throwable cause = e;
        while (cause.getCause() != null && cause != cause.getCause()) {
            if (cause instanceof FeignException) break;
            cause = cause.getCause();
        }
        return cause;
    }
}


