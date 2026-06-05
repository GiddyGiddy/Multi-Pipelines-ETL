package com.ideaproject.etl.business.dataprovider;

/**
 * Transforms one representation into another.
 *
 * @param <I> input type
 * @param <O> output type
 */
public interface Transformer<I, O> {

    O transform(I input);
}

