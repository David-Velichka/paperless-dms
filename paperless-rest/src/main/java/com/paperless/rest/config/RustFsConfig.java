package com.paperless.rest.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class RustFsConfig {

    @Value("${rustfs.endpoint:http://localhost:9000}")
    private String endpoint;

    @Value("${rustfs.access-key:rustfsadmin}")
    private String accessKey;

    @Value("${rustfs.secret-key:rustfsadmin}")
    private String secretKey;

    @Value("${rustfs.bucket-name:paperless-documents}")
    private String bucketName;
}
