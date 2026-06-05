package com.ideaproject.etl.controller;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * All routes of the dispatcher as an overview and to avoid misspellings in security config.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Routes {
    public static final String API_BASE_PATH = "/api";
    public static final String PIPELINE_BASE_PATH = API_BASE_PATH + "/pipelines";
    public static final String START_EXECUTION = "/start";
    public static final String STOP = "/stop";
    public static final String STATUS = "/status";

    // Backward-compatible aliases for existing route usages.
    public static final String UPDATE_BASE_PATH = PIPELINE_BASE_PATH;
    public static final String START = START_EXECUTION;
}
