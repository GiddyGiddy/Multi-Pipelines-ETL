package com.ideaproject.etl.business.dto;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for pipeline execution status.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PipelineStatusResponseDto {

    private boolean isRunning;

    private Date updateStartedAt;

    private Date updateCanceledAt;

    private Date lastSuccessfullUpdate;

    private String pipeline01EntityStatus;

    private String pipeline02EntityStatus;

    private List<PipelineStatusDto> pipelines;
}

