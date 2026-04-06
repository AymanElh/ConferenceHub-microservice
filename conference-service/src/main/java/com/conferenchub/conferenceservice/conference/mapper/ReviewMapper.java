package com.conferenchub.conferenceservice.conference.mapper;

import com.conferenchub.conferenceservice.conference.dto.request.CreateReviewDto;
import com.conferenchub.conferenceservice.conference.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conference", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "date", ignore = true)
    Review toEntity(CreateReviewDto dto);
}
