package com.conferenchub.conferenceservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import feign.codec.ErrorDecoder;
import com.conferenchub.conferenceservice.conference.exception.KeynoteNotFoundException;
import org.springframework.context.annotation.Bean;

@EnableFeignClients
@SpringBootApplication
@EnableJpaAuditing
public class ConferenceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConferenceServiceApplication.class, args);
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            if (response.status() == 404) {
                return new KeynoteNotFoundException("Keynote with ID requested was not found");
            }
            return feign.FeignException.errorStatus(methodKey, response);
        };
    }

}
