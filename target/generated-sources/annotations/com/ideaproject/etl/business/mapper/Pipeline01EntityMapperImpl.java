package com.ideaproject.etl.business.mapper;

import com.ideaproject.etl.business.dto.event.Pipeline01EntityItemDto;
import com.ideaproject.etl.domain.entities.entity1.Pipeline01Entity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-28T17:57:54+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.200.v20261008-1115, environment: Java 25.0.4.1 (Eclipse Adoptium)"
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
