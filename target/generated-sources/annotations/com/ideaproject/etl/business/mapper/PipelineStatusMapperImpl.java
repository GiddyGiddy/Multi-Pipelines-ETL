package com.ideaproject.etl.business.mapper;

import com.ideaproject.etl.business.dto.PipelineStatusResponseDto;
import com.ideaproject.etl.domain.entities.PipelineExecutionStatus;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-05T22:00:21+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Eclipse Adoptium)"
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
        pipelineStatusResponseDto.updateStartedAt( pipelineExecutionStatus.getUpdateStartedAt() );
        pipelineStatusResponseDto.updateCanceledAt( pipelineExecutionStatus.getUpdateCanceledAt() );
        pipelineStatusResponseDto.lastSuccessfullUpdate( pipelineExecutionStatus.getLastSuccessfullUpdate() );

        pipelineStatusResponseDto.pipeline01EntityStatus( pipelineExecutionStatus.getPipeline01ProceedStatus().toString() );
        pipelineStatusResponseDto.pipeline02EntityStatus( pipelineExecutionStatus.getPipeline02ProceedStatus().toString() );
        pipelineStatusResponseDto.pipelines( mapPipelines(pipelineExecutionStatus) );

        return pipelineStatusResponseDto.build();
    }
}
