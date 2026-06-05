package com.ideaproject.etl.business.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic status representation for a pipeline job.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PipelineStatusDto {

    private String pipelineId;

    private long totalItems;

    private long currentItem;

    private String status;
}

