package com.ideaproject.etl.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

/**
 * Configuration for SQS Client
 */
@Configuration
public class SqsConfiguration {

    @Autowired
    private Environment environment;

    /**
     * Builds the SQS Client used for interacting with the Queues in the Integration Environment
     *
     * @return The SQS Client
     */
    @Bean
    @Profile("!dev")
    public SqsClient sqsClient() {
        return SqsClient.builder().build();
    }

    /**
     * Builds the SQS Client used for interacting with the Queues in the Development Environment
     * @return The SQS Client
     */
    @Bean
    @Profile("dev")
    public SqsClient sqsClientDev() {
        return SqsClient.builder()
                .region(Region.of(environment.getProperty("spring.cloud.aws.region.static")))
                .endpointOverride(URI.create(environment.getProperty("spring.cloud.aws.sqs.endpoint")))
                .credentialsProvider(() -> new AwsCredentials() {
                    @Override
                    public String accessKeyId() {
                        return environment.getProperty("spring.cloud.aws.credentials.accessKey");
                    }

                    @Override
                    public String secretAccessKey() {
                        return environment.getProperty("spring.cloud.aws.credentials.secretKey");
                    }
                })
                .build();
    }
}

