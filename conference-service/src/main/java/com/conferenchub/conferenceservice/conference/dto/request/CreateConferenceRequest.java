package com.conferenchub.conferenceservice.conference.dto.request;

import com.conferenchub.conferenceservice.conference.entity.ConferenceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateConferenceRequest {
    @NotBlank
    private String title;

    @NotNull
    private ConferenceType type;

    @NotNull
    private LocalDate date;

    @Min(1)
    private Integer duration;

    @NotEmpty(message = "At least one keynote is required")
    private List<Long> keynoteIds;
}
