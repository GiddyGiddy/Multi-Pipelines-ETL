package com.ideaproject.etl.business.dto.event;

import java.util.ArrayList;
import java.util.List;

/**
 * Event envelope for Entity1 payload.
 */
public class Pipeline01EntityTransfer {

    private final List<Pipeline01EntityItemDto> payload = new ArrayList<>();

    public List<Pipeline01EntityItemDto> getPayload() {
        return payload;
    }
}
