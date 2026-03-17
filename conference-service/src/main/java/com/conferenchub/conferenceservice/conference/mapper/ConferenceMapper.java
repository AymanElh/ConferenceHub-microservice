package com.conferenchub.conferenceservice.conference.mapper;

import com.conferenchub.conferenceservice.conference.dto.request.CreateConferenceRequest;
import com.conferenchub.conferenceservice.conference.dto.response.ConferenceResponse;
import com.conferenchub.conferenceservice.conference.entity.Conference;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConferenceMapper {

    Conference toEntity(CreateConferenceRequest dto);

    ConferenceResponse toResponse(Conference conference);

    List<ConferenceResponse> toResponseList(List<Conference> conferences);
}
