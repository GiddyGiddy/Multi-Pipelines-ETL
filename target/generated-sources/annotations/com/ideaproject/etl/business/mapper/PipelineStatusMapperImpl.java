package com.ideaproject.etl.business.mapper;

import com.ideaproject.etl.business.dto.PipelineStatusResponseDto;
import com.ideaproject.etl.domain.entities.PipelineExecutionStatus;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-28T17:57:54+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.200.v20261008-1115, environment: Java 25.0.4.1 (Eclipse Adoptium)"
)
@Component
public class PipelineStatusMapperImpl extends PipelineStatusMapper {

    @Override
    public PipelineStatusResponseDto toDto(PipelineExecutionStatus pipelineExecutionStatus) {
        if ( pipelineExecutionStatus == null ) {
            return null;
        }

        PipelineStatusResponseDto.PipelineStatusResponseDtoBuilder pipelineStatusResponseDto = PipelineStatusResponseDto.builder();

        pipelineStatusResponseDto.isRunning( pipelineExecutionStatus.isRunning() );
        pipelineStatusResponseDto.lastSuccessfullUpdate( pipelineExecutionStatus.getLastSuccessfullUpdate() );
        pipelineStatusResponseDto.updateCanceledAt( pipelineExecutionStatus.getUpdateCanceledAt() );
        pipelineStatusResponseDto.updateStartedAt( pipelineExecutionStatus.getUpdateStartedAt() );

        pipelineStatusResponseDto.pipeline01EntityStatus( pipelineExecutionStatus.getPipeline01ProceedStatus().toString() );
        pipelineStatusResponseDto.pipeline02EntityStatus( pipelineExecutionStatus.getPipeline02ProceedStatus().toString() );
        pipelineStatusResponseDto.pipelines( mapPipelines(pipelineExecutionStatus) );

        return pipelineStatusResponseDto.build();
    }
}
