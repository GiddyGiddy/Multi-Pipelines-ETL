package com.ideaproject.etl.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.UUID;

/**
 * Implementation of the MessagePublisher Interface to publish messages to SQS
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "etl.messaging.transport", havingValue = "sqs", matchIfMissing = true)
public class MessagePublisherSqsClient implements MessagePublisher {

    @Autowired
    private SqsClient sqsClient;

    @Override
    public boolean publish(String bindingName, Object message) {
        SendMessageRequest messageRequest = SendMessageRequest.builder()
                .queueUrl(bindingName)
                .messageBody(message.toString())
                .messageGroupId("dispatcher")
                .messageDeduplicationId(UUID.randomUUID().toString()).build();

        try {
            sqsClient.sendMessage(messageRequest);
        } catch (SdkException ex) {
            log.error("Error publishing message: {}" + ex, message);
            return false;
        }
        return true;
    }
}

