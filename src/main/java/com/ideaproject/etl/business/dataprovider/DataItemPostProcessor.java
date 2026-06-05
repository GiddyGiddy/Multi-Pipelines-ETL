package com.ideaproject.etl.business.dataprovider;

import java.util.function.Consumer;

/**
 * DataItem PostProcessor functional interface
 * @param <T> generic data type for the consumer
 */
@FunctionalInterface
public interface DataItemPostProcessor<T> extends Consumer<T> {
    default void accept(T t) {
    }

    /**
     * Post process a concrete consumer data item
     * @param t consumer data item
     * @param index_from index of the consumer data item
     * @param index_to index of the consumer data item
     * @param size size of all consumer data items
     */
    void postProcess(T t, long index_from, long index_to, long size);
}

