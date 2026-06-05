package com.ideaproject.etl.business.pipeline;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Job identifiers for all configured dispatcher pipelines.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineJobId {
    public static final String PIPELINE_01_ENTITY = "pipeline-01-entity";
    public static final String PIPELINE_02_ENTITY = "pipeline-02-entity";
}

