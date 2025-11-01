package com.found404.marketbee.storeVerify.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class ClovaOcrClient {

    private final WebClient webClient;
    private final String clientSecret;

    public ClovaOcrClient(
            @Value("${external.clova.ocr-url}") String ocrUrl,
            @Value("${external.clova.client-secret}") String clientSecret
    ) {
        this.clientSecret = clientSecret;
        this.webClient = WebClient.builder()
                .baseUrl(ocrUrl)
                .build();
    }

    public Map<String, Object> requestOcr(MultipartFile imageFile) {
        try {
            String originalName = imageFile.getOriginalFilename();
            if (originalName == null || !originalName.contains(".")) {
                throw new IllegalArgumentException("파일 확장자를 확인할 수 없습니다.");
            }

            String ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
            if (!ext.equals("jpg") && !ext.equals("png")) {
                throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다. (jpg, png만 가능)");
            }

            String base64Image = Base64.getEncoder().encodeToString(imageFile.getBytes());

            Map<String, Object> requestBody = Map.of(
                    "version", "V2",
                    "requestId", UUID.randomUUID().toString(),
                    "timestamp", System.currentTimeMillis(),
                    "images", List.of(
                            Map.of(
                                    "format", ext,
                                    "name", "store_license",
                                    "data", base64Image
                            )
                    )
            );

            Map<String, Object> response = webClient.post()
                    .header("X-OCR-SECRET", clientSecret)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            log.info("Clova OCR 응답: {}", response);
            return response;

        } catch (WebClientResponseException e) {
            log.error("Clova OCR API 호출 실패: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Clova OCR API 호출 실패", e);
        } catch (Exception e) {
            log.error("Clova OCR 처리 중 오류", e);
            throw new RuntimeException("Clova OCR 처리 중 오류", e);
        }
    }
}