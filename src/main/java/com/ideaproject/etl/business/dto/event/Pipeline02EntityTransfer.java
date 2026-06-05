package com.ideaproject.etl.business.dto.event;

import java.util.ArrayList;
import java.util.List;

/**
 * Event envelope for Entity2 payload.
 */
public class Pipeline02EntityTransfer {

    private final List<Pipeline02EntityItemDto> payload = new ArrayList<>();

    public List<Pipeline02EntityItemDto> getPayload() {
        return payload;
    }
}
