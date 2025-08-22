package com.found404.marketbee.sns.template.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageAiService {

    private final RestClient openAiRestClient;
    private final S3Client s3Client;

    @Value("${openai.image-model:gpt-image-1}")
    private String model;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.base-url}")
    private String baseUrl;

    @Value("${cloud.aws.region.static:ap-northeast-2}")
    private String region;

    /** S3 “폴더” 선택지 */
    public enum Folder {
        BACKGROUNDS("backgrounds"),
        FINAL("final");

        private final String prefix;
        Folder(String prefix) { this.prefix = prefix; }
        public String prefix() { return prefix; }
    }


    public String generate(String prompt, String size) {
        return generate(prompt, size, Folder.BACKGROUNDS);
    }


    public String generate(String prompt, String size, Folder folder) {
        final String safeSize = switch (size) {
            case "1024x1024", "1024x1536", "1536x1024", "auto" -> size;
            default -> "1024x1024";
        };

        var reqBody = Map.of(
                "model", model,
                "prompt", prompt,
                "size", safeSize,
                "n", 1
        );

        try {
            var entity = openAiRestClient.post()
                    .uri("/images/generations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.ALL)
                    .body(reqBody)
                    .retrieve()
                    .toEntity(byte[].class);

            MediaType ct = entity.getHeaders().getContentType();
            byte[] body = entity.getBody();

            byte[] imageBytes;
            if (ct != null && MediaType.APPLICATION_JSON.includes(ct)) {
                Map<?, ?> json = new ObjectMapper().readValue(body, Map.class);
                var data = (List<Map<String, Object>>) json.get("data");
                if (data == null || data.isEmpty()) throw new IllegalStateException("Image API returned no data");
                var first = data.get(0);
                String url = (String) first.get("url");
                if (url != null && !url.isBlank()) {
                    imageBytes = new URL(url).openStream().readAllBytes();
                } else {
                    String b64 = (String) first.get("b64_json");
                    if (b64 == null) throw new IllegalStateException("No url/b64_json in response");
                    if (b64.startsWith("data:")) {
                        int comma = b64.indexOf(',');
                        if (comma != -1) b64 = b64.substring(comma + 1);
                    }
                    imageBytes = Base64.getDecoder().decode(b64);
                }
            } else {
                imageBytes = body;
            }

            return uploadToS3AndReturnUrl(imageBytes, folder);

        } catch (IOException e) {
            throw new RuntimeException("이미지 처리 실패", e);
        } catch (Exception e) {
            throw new RuntimeException("OpenAI 이미지 생성 실패", e);
        }
    }

    private String uploadToS3AndReturnUrl(byte[] imageBytes, Folder folder) {
        try {
            String key = "%s/%s/img_%d.png".formatted(
                    folder.prefix(),
                    LocalDate.now(),
                    System.currentTimeMillis()
            );

            PutObjectRequest req = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType("image/png")
                    .build();

            s3Client.putObject(req, RequestBody.fromBytes(imageBytes));
            return baseUrl + "/" + key;

        } catch (Exception e) {
            throw new RuntimeException("이미지 S3 업로드 실패", e);
        }
    }
}
