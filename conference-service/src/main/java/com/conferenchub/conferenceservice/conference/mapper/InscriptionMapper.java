package com.conferenchub.conferenceservice.conference.mapper;

import com.conferenchub.conferenceservice.conference.dto.request.CreateInscriptionDto;
import com.conferenchub.conferenceservice.conference.entity.Inscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InscriptionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conference", ignore = true)
    @Mapping(target = "dateInscription", ignore = true)
    @Mapping(target = "status", ignore = true)
    Inscription toEntity(CreateInscriptionDto dto);
}
