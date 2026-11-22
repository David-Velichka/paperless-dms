package com.paperless.rest.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(RustFsProperties.class)
public class RustFsConfig {

    // RustFS speaks the S3 protocol: custom endpoint, path style and a fixed dummy region
    @Bean
    public S3Client s3Client(RustFsProperties properties) {
        return S3Client.builder()
                .endpointOverride(URI.create(properties.endpoint()))
                .region(Region.US_EAST_1)
                .forcePathStyle(true)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())))
                // The built-in SDK retry (3 attempts with backoff) stays active, the timeouts cap each attempt and the call
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .apiCallTimeout(properties.apiCallTimeout())
                        .apiCallAttemptTimeout(properties.apiCallAttemptTimeout())
                        .build())
                .build();
    }
}
