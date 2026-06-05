package com.ideaproject.etl.business.mapper;

import com.ideaproject.etl.business.dto.event.Pipeline02EntityItemDto;
import com.ideaproject.etl.domain.entities.Entity2.Pipeline02Entity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-05T22:00:21+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class Pipeline02EntityMapperImpl implements Pipeline02EntityMapper {

    @Override
    public Pipeline02EntityItemDto mapToPipeline02EntityItemDto(Pipeline02Entity pipeline02Entity) {
        if ( pipeline02Entity == null ) {
            return null;
        }

        Pipeline02EntityItemDto pipeline02EntityItemDto = new Pipeline02EntityItemDto();

        return pipeline02EntityItemDto;
    }
}
