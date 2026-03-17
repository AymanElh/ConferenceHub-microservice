package org.example.keynoteservice.Mappers;

import org.example.keynoteservice.dto.KeynoteDTO;
import org.example.keynoteservice.model.Keynote;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface KeynoteMapper {
    KeynoteDTO toDto(Keynote keynote);
    Keynote toEntity(KeynoteDTO keynoteDTO);
}
