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

    private static final String DEFAULT_MODEL = "gpt-4o-mini";

    private final PhotoRepository photoRepository;
    private final OpenAiClient openAiClient;

    public String analyze(Long photoId, String model) {
        if (photoId == null) throw new IllegalArgumentException("photoId is required");
        String useModel = (model == null || model.isBlank()) ? DEFAULT_MODEL : model;

        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("photo not found: " + photoId));

        String userContent =
                "Analyze this product photo for listing quality.\n" +
                        "File URL: " + photo.getFileUrl() + "\n" +
                        "Check: focus, exposure, blur, reflections, background clutter, cropping, aspect ratio.\n" +
                        "Return a short bullet list and a final pass/fail.";

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", "You are a concise photo QA assistant for e-commerce."),
                Map.of("role", "user", "content", userContent)
        );

        Map<String, Object> res = openAiClient.chatRaw((List) messages, useModel, 0.2, 800);
        return extractContent(res);
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> res) {
        if (res == null) return "No response";
        Object choicesObj = res.get("choices");
        if (choicesObj instanceof List<?> choices && !choices.isEmpty()) {
            Object first = choices.get(0);
            if (first instanceof Map<?, ?> m) {
                Object message = m.get("message");
                if (message instanceof Map<?, ?> msg) {
                    Object content = msg.get("content");
                    if (content != null) return content.toString();
                }
                Object text = m.get("text");
                if (text != null) return text.toString();
            }
        }
        return res.toString();
    }
}
