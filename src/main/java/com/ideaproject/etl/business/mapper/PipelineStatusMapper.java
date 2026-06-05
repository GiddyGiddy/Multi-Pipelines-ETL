package com.ideaproject.etl.business.mapper;

import com.ideaproject.etl.business.dto.PipelineStatusDto;
import com.ideaproject.etl.business.dto.PipelineStatusResponseDto;
import com.ideaproject.etl.business.pipeline.PipelineJobDefinition;
import com.ideaproject.etl.business.pipeline.PipelineJobRegistry;
import com.ideaproject.etl.domain.entities.ProceedStatus;
import com.ideaproject.etl.domain.entities.PipelineExecutionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps the internal pipeline execution status to the API status response.
 */
@Mapper(componentModel = "spring")
public abstract class PipelineStatusMapper {

        @Autowired
        protected PipelineJobRegistry pipelineJobRegistry;

        /**
         * Maps pipeline execution status to a response DTO.
         *
         * @param pipelineExecutionStatus current pipeline execution status
         * @return mapped pipeline status response
         */
    @Mapping(target = "isRunning", source = "running")
        @Mapping(target = "pipeline01EntityStatus", expression = "java(pipelineExecutionStatus.getPipeline01ProceedStatus().toString())")
        @Mapping(target = "pipeline02EntityStatus", expression = "java(pipelineExecutionStatus.getPipeline02ProceedStatus().toString())")
                @Mapping(target = "pipelines", expression = "java(mapPipelines(pipelineExecutionStatus))")
        public abstract PipelineStatusResponseDto toDto(PipelineExecutionStatus pipelineExecutionStatus);

        protected List<PipelineStatusDto> mapPipelines(PipelineExecutionStatus pipelineExecutionStatus) {
                List<PipelineStatusDto> pipelines = new ArrayList<>();
                List<PipelineJobDefinition> jobs = pipelineJobRegistry.getJobsInOrder();
                for (int i = 0; i < jobs.size(); i++) {
                        ProceedStatus proceedStatus = getProceedStatusByIndex(pipelineExecutionStatus, i);
                        pipelines.add(PipelineStatusDto.builder()
                                        .pipelineId(jobs.get(i).getId())
                                        .totalItems(proceedStatus.getTotalItems())
                                        .currentItem(proceedStatus.getCurrentItem())
                                        .status(proceedStatus.toString())
                                        .build());
                }
                return pipelines;
        }

        protected ProceedStatus getProceedStatusByIndex(PipelineExecutionStatus pipelineExecutionStatus, int index) {
                return switch (index) {
                        case 0 -> pipelineExecutionStatus.getPipeline01ProceedStatus();
                        case 1 -> pipelineExecutionStatus.getPipeline02ProceedStatus();
                        default -> new ProceedStatus(0, 0);
                };
        }
}

