package com.ideaproject.etl.messaging;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * Implementation of the MessagePublisher Interface to publish
 * messages to a StreamBridge
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "etl.messaging.transport", havingValue = "kafka")
public class MessagePublisherStreamBridge implements MessagePublisher {

    private final StreamBridge streamBridge;

    /**
     * Constructor for MessagePublisherStreamBridge
     * @param streamBridge StreamBridge
     */
    public MessagePublisherStreamBridge(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Override
    public boolean publish(String bindingName, Object message) {
        boolean sent;
        try {
            sent = streamBridge.send(bindingName,
                    MessageBuilder.withPayload(message).build());
        }catch (Exception e){
            log.error("description = {} cause = {}", ExceptionUtils.getMessage(e),
                    ExceptionUtils.getRootCauseMessage(e));
            return false;
        }
        return sent;
    }
}

