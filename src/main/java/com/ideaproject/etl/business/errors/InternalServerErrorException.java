package com.ideaproject.etl.business.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Handles Internal Server Error Exception
 */
public class InternalServerErrorException extends ResponseStatusException {

    /**
     * Internal Server Error Constructor
     *
     * @param ex The thrown Exception
     */
    public InternalServerErrorException(Exception ex) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error: " + ex.getMessage(), ex);
    }
}

