package com.ideaproject.etl.business.pipeline;

import com.ideaproject.etl.business.dataprovider.Pipeline01EntityProvider;
import com.ideaproject.etl.business.dataprovider.Pipeline02EntityProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registry containing all executable pipeline jobs and their runtime bindings.
 */
@Component
public class PipelineJobRegistry {

    private final List<PipelineJobDefinition> jobsInOrder;
    private final Map<String, PipelineJobDefinition> jobsById;

    public PipelineJobRegistry(
            Pipeline01EntityProvider pipeline01Provider,
            Pipeline02EntityProvider pipeline02Provider) {
        this.jobsInOrder = List.of(
                new PipelineJobDefinition(
                        PipelineJobId.PIPELINE_01_ENTITY,
                pipeline01Provider),
                new PipelineJobDefinition(
                        PipelineJobId.PIPELINE_02_ENTITY,
                pipeline02Provider));
        this.jobsById = this.jobsInOrder.stream()
                .collect(Collectors.toUnmodifiableMap(PipelineJobDefinition::getId, Function.identity()));
    }

    public List<PipelineJobDefinition> getJobsInOrder() {
        return jobsInOrder;
    }

    public Optional<PipelineJobDefinition> findById(String id) {
        return Optional.ofNullable(jobsById.get(id));
    }
}

