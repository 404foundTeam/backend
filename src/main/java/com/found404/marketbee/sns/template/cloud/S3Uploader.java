package com.found404.marketbee.sns.template.cloud;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.base-url}")
    private String baseUrl;

    @Value("${cloud.aws.region.static:ap-northeast-2}")
    private String region;

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    private final S3Client s3Client;

    public String uploadPng(byte[] bytes, String keyPrefix) {
        String key = "%s/%s/img_%d.png".formatted(
                keyPrefix, LocalDate.now(), System.currentTimeMillis());

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("image/png")
                .cacheControl("public, max-age=31536000, immutable")
                .build();

        s3Client.putObject(req, RequestBody.fromBytes(bytes));
        return baseUrl + "/" + key;
    }


    public URL generatePresignedPutUrl(String keyPrefix) {
        String key = "%s/%s/final_%d.png".formatted(
                keyPrefix, LocalDate.now(), System.currentTimeMillis());

        S3Presigner presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("image/png")
                .build();

        PresignedPutObjectRequest presignedRequest =
                presigner.presignPutObject(r -> r.signatureDuration(Duration.ofMinutes(5))
                        .putObjectRequest(objectRequest));

        return presignedRequest.url(); // java.net.URL 반환
    }
}
