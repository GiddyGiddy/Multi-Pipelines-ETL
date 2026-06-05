package com.ideaproject.etl.messaging;

/**
 * Interface to publish messages to a generic Queue
 */
public interface MessagePublisher {

    /**
     * Publish a message to a given Queue
     * @param bindingName Name of the Queue
     * @param message Message to send
     * @return boolean specifying if the message was successful or not
     */
    boolean publish(String bindingName, Object message);
}

