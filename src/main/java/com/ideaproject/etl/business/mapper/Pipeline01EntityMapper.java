package com.ideaproject.etl.business.mapper;

import com.ideaproject.etl.business.dto.event.Pipeline01EntityItemDto;
import com.ideaproject.etl.domain.entities.entity1.Pipeline01Entity;

import org.mapstruct.Mapper;

/**
 * Enables mapping of pipeline 01 entities to payload DTOs.
 */
@Mapper(componentModel = "spring")
public interface Pipeline01EntityMapper {

    /**
     * Maps a pipeline 01 entity to DTO.
     * @param pipeline01Entity the entity to map
     * @return the mapped payload DTO
     */
    Pipeline01EntityItemDto mapToPipeline01EntityItemDto(Pipeline01Entity pipeline01Entity);

}

