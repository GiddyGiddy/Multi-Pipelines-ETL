package com.ideaproject.etl.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Domain status holder for pipeline execution progress.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Component
public class PipelineExecutionStatus {
    private boolean running;

    private Date updateStartedAt;

    private Date updateCanceledAt;

    private Date lastSuccessfullUpdate;

    private ProceedStatus pipeline01ProceedStatus;

    private ProceedStatus pipeline02ProceedStatus;
}

