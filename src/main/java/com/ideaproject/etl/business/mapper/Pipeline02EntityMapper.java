package com.ideaproject.etl.business.mapper;

import com.ideaproject.etl.business.dto.event.Pipeline02EntityItemDto;
import com.ideaproject.etl.domain.entities.Entity2.Pipeline02Entity;
import org.mapstruct.Mapper;

/**
 * Enable the mapping of Pipeline02Entity to Pipeline02EntityDto.
 */
@Mapper(componentModel = "spring")
public interface Pipeline02EntityMapper {

    Pipeline02EntityItemDto mapToPipeline02EntityItemDto(Pipeline02Entity pipeline02Entity);
}
