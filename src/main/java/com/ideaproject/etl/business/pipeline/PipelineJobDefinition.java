package com.ideaproject.etl.business.pipeline;

import com.ideaproject.etl.business.dataprovider.DataProvider;
import lombok.Getter;

/**
 * Immutable pipeline job definition.
 */
@Getter
public class PipelineJobDefinition {
    private final String id;
    private final DataProvider<?, ?> provider;

    public PipelineJobDefinition(String id, DataProvider<?, ?> provider) {
        this.id = id;
        this.provider = provider;
    }
}

