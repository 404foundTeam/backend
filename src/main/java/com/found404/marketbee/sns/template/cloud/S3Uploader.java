package com.found404.marketbee.sns.template.cloud;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.base-url}")
    private String baseUrl;

    private final S3Client s3Client;

    public String uploadPng(byte[] bytes, String keyPrefix) {
        String key = "%s/%s/img_%d.png".formatted(keyPrefix, LocalDate.now(), System.currentTimeMillis());

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("image/png")
                .cacheControl("public, max-age=31536000, immutable")
                .build();

        s3Client.putObject(req, RequestBody.fromBytes(bytes));
        return baseUrl + "/" + key;
    }
}