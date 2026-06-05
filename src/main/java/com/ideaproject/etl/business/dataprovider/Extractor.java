package com.ideaproject.etl.business.dataprovider;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Extracts source data in pages.
 *
 * @param <T> source type
 */
public interface Extractor<T> {

    Page<T> extract(Pageable pageable);

    long count();
}

