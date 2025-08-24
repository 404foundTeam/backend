package com.found404.marketbee.photo.analysis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class GuideTextService {

    @Value("${openai.api.key}")   // << 여기만 변경
    private String apiKey;

    private static final ObjectMapper M = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    public String fromFile(MultipartFile file) throws Exception {
        String mime = file.getContentType() != null ? file.getContentType() : "image/png";
        String b64 = Base64.getEncoder().encodeToString(file.getBytes());
        String dataUrl = "data:" + mime + ";base64," + b64;
        return callOpenAI(dataUrl);
    }

    public String fromDataUrl(String dataUrl) throws Exception {
        return callOpenAI(dataUrl);
    }

    private String callOpenAI(String dataUrl) throws Exception {
        ObjectNode root = M.createObjectNode();
        root.put("model", "gpt-4o-mini");

        ArrayNode messages = root.putArray("messages");

        ObjectNode sys = M.createObjectNode();
        sys.put("role", "system");
        sys.put("content",
                "너는 매장 사진 촬영 코치다. 출력은 한국어 3~4줄로만, 불릿/이모지/머리말 없이 간결하게. "
                        + "1) 현재 사진 평가 2) 개선 팁 3) 재촬영 시 한 줄 지침. 240자 이내.");
        messages.add(sys);

        ObjectNode user = M.createObjectNode();
        user.put("role", "user");
        ArrayNode content = user.putArray("content");

        ObjectNode t = M.createObjectNode();
        t.put("type", "text");
        t.put("text", "아래 이미지를 보고 매장 사진 촬영 가이드 텍스트를 3~4줄로 작성해줘.");
        content.add(t);

        ObjectNode img = M.createObjectNode();
        img.put("type", "image_url");
        ObjectNode iu = img.putObject("image_url");
        iu.put("url", dataUrl);
        content.add(img);

        messages.add(user);

        String json = M.writeValueAsString(root);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() / 100 != 2) {
            throw new IllegalStateException("OpenAI error: " + res.statusCode() + " - " + res.body());
        }

        JsonNode j = M.readTree(res.body());
        String out = j.path("choices").get(0).path("message").path("content").asText();
        if (out == null || out.isBlank()) throw new IllegalStateException("empty content");
        return out.trim();
    }
}
