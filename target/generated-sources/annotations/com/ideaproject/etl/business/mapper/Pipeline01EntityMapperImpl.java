package com.ideaproject.etl.business.mapper;

import com.ideaproject.etl.business.dto.event.Pipeline01EntityItemDto;
import com.ideaproject.etl.domain.entities.entity1.Pipeline01Entity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-05T22:00:21+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class Pipeline01EntityMapperImpl implements Pipeline01EntityMapper {

    @Override
    public Pipeline01EntityItemDto mapToPipeline01EntityItemDto(Pipeline01Entity pipeline01Entity) {
        if ( pipeline01Entity == null ) {
            return null;
        }

        Pipeline01EntityItemDto pipeline01EntityItemDto = new Pipeline01EntityItemDto();

        return pipeline01EntityItemDto;
    }
}
