package com.ideaproject.etl.business.service;

import com.ideaproject.etl.business.dataprovider.DataItemPostProcessor;
import com.ideaproject.etl.business.errors.TechnicalException;
import com.ideaproject.etl.business.pipeline.PipelineJobDefinition;
import com.ideaproject.etl.business.pipeline.PipelineJobRegistry;
import com.ideaproject.etl.domain.entities.ProceedStatus;
import com.ideaproject.etl.domain.entities.PipelineExecutionStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * Service for executing registered pipeline jobs and publishing their payloads.
 */
@Service
@Slf4j
public class PipelineExecutionService {
    private final PipelineExecutionStatus pipelineExecutionStatus;
    private final PipelineJobRegistry pipelineJobRegistry;

    @Value("${dispatcher.pagination.size:50}")
    private String pagingSize;

    /**
     * Constructor for pipeline execution service.
     * @param statusState current pipeline execution status holder
     */
    public PipelineExecutionService(
            PipelineExecutionStatus statusState,
            PipelineJobRegistry pipelineJobRegistry) {
        this.pipelineExecutionStatus = statusState;
        this.pipelineJobRegistry = pipelineJobRegistry;

        if (statusState.getPipeline01ProceedStatus() == null) {
            statusState.setPipeline01ProceedStatus(new ProceedStatus(0, 0));
        }
        if (statusState.getPipeline02ProceedStatus() == null) {
            statusState.setPipeline02ProceedStatus(new ProceedStatus(0, 0));
        }
    }

    private void initializeStatus() {
        pipelineExecutionStatus.setRunning(true);
        pipelineExecutionStatus.setUpdateStartedAt(new Date());
        pipelineExecutionStatus.setUpdateCanceledAt(null);
        List<PipelineJobDefinition> jobs = pipelineJobRegistry.getJobsInOrder();
        for (int i = 0; i < jobs.size(); i++) {
            setStatusByIndex(i, new ProceedStatus(jobs.get(i).getProvider().getItemsCount(), 0));
        }
    }

    public PipelineExecutionStatus getStatus() {
        return pipelineExecutionStatus;
    }

    /**
     * Starts sending messages to the queues
     * @param parallelExecution boolean representing whether this should happen in parallel or not
     * @return either parallel or non parallel sending of messages
     */
    public CompletableFuture<Boolean> startExecution(boolean parallelExecution) {
        log.debug("startExecution");
        int pageSize = Integer.parseInt(pagingSize);
        initializeStatus();
        if (parallelExecution) {
            return executeParallel(pageSize);
        } else {
            return executeSequential(pageSize);
        }
    }

    /**
     * Executes the loading of data sequential
     * @param pageSize size of page to use
     * @return true if the execution is finished without issues
     */
    private CompletableFuture<Boolean> executeSequential(int pageSize) {
        CompletableFuture<Boolean> future = CompletableFuture.completedFuture(true);
        for (PipelineJobDefinition job : pipelineJobRegistry.getJobsInOrder()) {
            future = future.thenApplyAsync(s -> s && runJob(job, pageSize));
        }
        return future.thenApplyAsync(this::finalizeUpdate);
    }
    /**
     * Executes the loading of data parallel
     * @param pageSize size of page to use
     * @return true if the execution is finished without issues
     */
    private CompletableFuture<Boolean> executeParallel(int pageSize) {
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        for (PipelineJobDefinition job : pipelineJobRegistry.getJobsInOrder()) {
            futures.add(CompletableFuture.supplyAsync(() -> runJob(job, pageSize)));
        }

        CompletableFuture<Void> combinedCF = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        return combinedCF.thenApplyAsync(x -> complete(futures));
    }

    /**
     * Checks if all pipeline futures are finished successfully.
     * @param jobFutures completable futures for all registered jobs
     * @return true if all jobs are finished properly otherwise false
     */
    protected Boolean complete(List<CompletableFuture<Boolean>> jobFutures) {

        try {
            boolean success = true;
            for (CompletableFuture<Boolean> jobFuture : jobFutures) {
                success = success && jobFuture.get();
            }
            return finalizeUpdate(success);
        } catch (InterruptedException | ExecutionException e) {
            throw new TechnicalException(e);
        }
    }

    /**
      * Stops execution and requests all registered providers to stop reading.
      * @return Current pipeline execution status
     */
    public PipelineExecutionStatus stopExecution() {
          log.debug("stopExecution");
        if (pipelineExecutionStatus.isRunning()) {
            pipelineExecutionStatus.setUpdateCanceledAt(new Date());
            pipelineExecutionStatus.setRunning(false);
            for (PipelineJobDefinition job : pipelineJobRegistry.getJobsInOrder()) {
                job.getProvider().stopReading();
            }
        }
        return pipelineExecutionStatus;
    }

    private boolean runJob(PipelineJobDefinition job, int pageSize) {
        Consumer<String> noOpPublisher = item -> { };
        return job.getProvider().startReading(noOpPublisher, pageSize, buildPostProcessor(job.getId()));
    }

    private Boolean finalizeUpdate(Boolean success) {
        pipelineExecutionStatus.setRunning(false);
        if (success) {
            pipelineExecutionStatus.setLastSuccessfullUpdate(new Date());
            pipelineExecutionStatus.setUpdateCanceledAt(null);
        } else {
            pipelineExecutionStatus.setLastSuccessfullUpdate(null);
            pipelineExecutionStatus.setUpdateCanceledAt(new Date());
        }
        long t = ((new Date().getTime()) - pipelineExecutionStatus.getUpdateStartedAt().getTime()) / 1000;
        log.info("Total time in seconds used for dispatching the messages: {}", t);
        return success;
    }

    private DataItemPostProcessor<String> buildPostProcessor(String pipelineId) {
        return (item, from, to, size) -> {
            log.debug("{}: [{}, {}]/{} -> {}", pipelineId, from, to, size, item);
            updateCurrentItem(pipelineId, to);
        };
    }

    private void updateCurrentItem(String pipelineId, long currentItem) {
        OptionalInt jobIndex = findJobIndex(pipelineId);
        if (jobIndex.isPresent()) {
            getStatusByIndex(jobIndex.getAsInt()).setCurrentItem(currentItem);
        } else {
            log.warn("Unknown pipeline id while updating progress: {}", pipelineId);
        }
    }

    private OptionalInt findJobIndex(String pipelineId) {
        List<PipelineJobDefinition> jobs = pipelineJobRegistry.getJobsInOrder();
        for (int i = 0; i < jobs.size(); i++) {
            if (jobs.get(i).getId().equals(pipelineId)) {
                return OptionalInt.of(i);
            }
        }
        return OptionalInt.empty();
    }

    private ProceedStatus getStatusByIndex(int index) {
        return switch (index) {
            case 0 -> pipelineExecutionStatus.getPipeline01ProceedStatus();
            case 1 -> pipelineExecutionStatus.getPipeline02ProceedStatus();
            default -> throw new IllegalArgumentException("Unsupported pipeline status index: " + index);
        };
    }

    private void setStatusByIndex(int index, ProceedStatus status) {
        switch (index) {
            case 0 -> pipelineExecutionStatus.setPipeline01ProceedStatus(status);
            case 1 -> pipelineExecutionStatus.setPipeline02ProceedStatus(status);
            default -> throw new IllegalArgumentException("Unsupported pipeline status index: " + index);
        }
    }

}

