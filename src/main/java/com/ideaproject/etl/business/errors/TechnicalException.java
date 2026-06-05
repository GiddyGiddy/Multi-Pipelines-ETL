package com.ideaproject.etl.business.errors;

/**
 * Handles Internal Server Error Exception
 */
public class TechnicalException extends RuntimeException{


    /**
     * Technical Exception Constructor
     *
     * @param ex The thrown Exception
     */
    public TechnicalException(Exception ex) {
        super(ex);
    }
}

