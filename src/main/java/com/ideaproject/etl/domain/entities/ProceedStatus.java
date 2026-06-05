package com.ideaproject.etl.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;


/**
 * ProceedStatus Entity
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Component
public class ProceedStatus {
    private long totalItems;

    private long currentItem;

    @Override
    public String toString() {
        return String.format("%s von %s", currentItem == 0 ? 0 : currentItem + 1, totalItems);
    }
}

