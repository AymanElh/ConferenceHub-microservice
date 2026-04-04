package com.conferenchub.conferenceservice.conference.dto.response;

import com.conferenchub.conferenceservice.conference.entity.ConferenceStatus;
import com.conferenchub.conferenceservice.conference.entity.ConferenceType;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ConferenceResponse {
    private Long id;
    private String title;
    private ConferenceStatus status;
    private ConferenceType type;
    private Integer duration;
    private Integer registeredNumber;
    private Double score;
    private LocalDate date;
    private List<KeynoteResponse> keynotes;
}
