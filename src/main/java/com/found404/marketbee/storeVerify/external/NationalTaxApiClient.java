package com.found404.marketbee.storeVerify.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class NationalTaxApiClient {

    private final WebClient webClient;
    private final String serviceKey;

    public NationalTaxApiClient(
            @Value("${external.nts.base-url}") String baseUrl,
            @Value("${external.nts.service-key}") String serviceKey
    ) {
        this.serviceKey = serviceKey;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public Map<String, String> verify(String storeNumber, String representativeName, String openDate) {
        try {
            String b_no = storeNumber.replaceAll("\\D", "");
            String p_nm = representativeName != null ? representativeName.trim() : "";
            String start_dt = openDate != null ? openDate.replaceAll("\\D", "") : "";

            Map<String, Object> requestBody = Map.of(
                    "businesses", List.of(Map.of(
                            "b_no", b_no,
                            "p_nm", p_nm,
                            "start_dt", start_dt
                    ))
            );

            Map<String, Object> response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/nts-businessman/v1/validate")
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("returnType", "JSON")
                            .build())
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .block();

            log.info("[NTS API Response] {}", response);

            if (response == null || !response.containsKey("data")) {
                return Map.of("valid", "99", "validMsg", "국세청 응답이 비어있습니다.");
            }

            List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
            if (data.isEmpty()) {
                return Map.of("valid", "99", "validMsg", "국세청 응답 데이터가 없습니다.");
            }

            Map<String, Object> result = data.get(0);
            String valid = (String) result.get("valid");
            String validMsg = (String) result.get("valid_msg");

            log.info("진위확인 결과: valid={}, message={}", valid, validMsg);

            return Map.of(
                    "valid", valid != null ? valid : "99",
                    "validMsg", validMsg != null ? validMsg : "응답 메시지 없음"
            );

        } catch (Exception e) {
            log.error("국세청 API 호출 실패", e);
            return Map.of("valid", "99", "validMsg", "국세청 API 호출 중 오류가 발생했습니다.");
        }
    }
}