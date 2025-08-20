package com.found404.marketbee.photo.analysis;

import com.found404.marketbee.photo.upload.Photo;
import com.found404.marketbee.photo.upload.PhotoRepository;
import com.found404.marketbee.sns.common.OpenAiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PhotoAnalyzeService {

    private final PhotoRepository photoRepository;
    private final OpenAiClient openAiClient;

    private static final String DEFAULT_MODEL = "gpt-4o-mini";

    public String analyze(Long photoId, String model, String extraGuideText) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("photo not found: " + photoId));

        String base = photo.getGuideText() == null ? "" : photo.getGuideText();
        String guide = extraGuideText == null || extraGuideText.isBlank()
                ? base
                : (base.isBlank() ? extraGuideText : base + "\n추가 가이드: " + extraGuideText);

        String prompt = """
                아래 상품 사진 가이드 준수 여부를 간결하게 평가해줘.
                - 파일 URL: %s
                - 가이드: %s
                출력 형식:
                - 종합평: (한 문장)
                - 체크리스트: (3~5개 불릿)
                - 개선팁: (2~3개 불릿)
                """.formatted(photo.getFileUrl(), guide.isBlank() ? "없음" : guide);

        List<Map<String, String>> messages = List.of(
                Map.of("role","system","content","You are a concise photo reviewer for commerce product shots."),
                Map.of("role","user","content", prompt)
        );

        Map<String,Object> res = openAiClient.chatRaw(
                messages,
                (model == null || model.isBlank()) ? DEFAULT_MODEL : model,
                0.3,
                600
        );

        List<Map<String,Object>> choices = (List<Map<String,Object>>) res.get("choices");
        Map<String,Object> message = (Map<String,Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }
}
