package com.conferenchub.conferenceservice.conference.dto.request;

import com.conferenchub.conferenceservice.conference.entity.ConferenceStatus;
import com.conferenchub.conferenceservice.conference.entity.ConferenceType;
import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateConferenceRequest {
    private String title;

    private ConferenceType type;

    private LocalDate date;

    private Integer duration;

    private ConferenceStatus status;
}
