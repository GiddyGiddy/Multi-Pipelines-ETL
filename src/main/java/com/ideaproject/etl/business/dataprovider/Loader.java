package com.ideaproject.etl.business.dataprovider;

/**
 * Loads a transformed payload into its destination.
 *
 * @param <T> payload type
 */
public interface Loader<T> {

    boolean load(T payload);
}

