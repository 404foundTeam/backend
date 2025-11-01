package com.found404.marketbee.storeVerify.external;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class OcrResultParser {

    public OcrData parse(Map<String, Object> response) {
        if (response == null)
            throw new IllegalArgumentException("OCR 응답이 비어있습니다.");

        List<Map<String, Object>> images = (List<Map<String, Object>>) response.get("images");
        if (images == null || images.isEmpty())
            throw new IllegalArgumentException("OCR 이미지 데이터가 없습니다.");

        List<Map<String, Object>> fields = (List<Map<String, Object>>) images.get(0).get("fields");
        if (fields == null || fields.isEmpty())
            throw new IllegalArgumentException("OCR 필드 데이터가 없습니다.");

        String storeNumber = "";
        String representativeName = "";
        String openDate = "";

        Pattern bizNumPattern = Pattern.compile("\\d{3}-?\\d{2}-?\\d{5}");
        Pattern datePattern = Pattern.compile("(19|20)\\d{2}[^0-9]*(0[1-9]|1[0-2])[^0-9]*(0[1-9]|[12][0-9]|3[01])");

        for (int i = 0; i < fields.size(); i++) {
            String text = ((String) fields.get(i).get("inferText")).replaceAll("\\s+", "");

            if (text.contains("등록번호") || text.contains("사업자등록번호")) {
                String candidate = extractNextNumber(fields, i, bizNumPattern);
                if (!candidate.isEmpty()) storeNumber = candidate;
            } else {
                Matcher m = bizNumPattern.matcher(text);
                if (m.find() && storeNumber.isEmpty()) {
                    storeNumber = m.group().replaceAll("-", "");
                }
            }

            if (representativeName.isEmpty()) {

                if (text.contains("성명") || text.contains("대표자")) {
                    representativeName = findNameNear(fields, i);
                }

                if (text.equals("성") && i + 2 < fields.size()) {
                    String next1 = ((String) fields.get(i + 1).get("inferText")).trim();
                    String next2 = ((String) fields.get(i + 2).get("inferText")).trim();
                    if (next1.equals("명") || (next1.equals("명:") || next2.equals(":"))) {
                        representativeName = findNameNear(fields, i + 2);
                    }
                }
            }

            if ((text.contains("개업연월일") || text.contains("개업일")) && openDate.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (int j = i + 1; j < Math.min(fields.size(), i + 7); j++) {
                    String next = ((String) fields.get(j).get("inferText")).trim();
                    sb.append(next);
                }

                Matcher m = datePattern.matcher(sb.toString());
                if (m.find()) openDate = normalizeDate(m.group());
            }
        }

        log.info("OCR 파싱 결과: 사업자등록번호={}, 대표자명={}, 개업일자={}",
                storeNumber, representativeName, openDate);

        return new OcrData(storeNumber, representativeName, openDate);
    }

    private String extractNextNumber(List<Map<String, Object>> fields, int currentIndex, Pattern bizNumPattern) {
        for (int j = currentIndex; j < Math.min(fields.size(), currentIndex + 4); j++) {
            String nextText = ((String) fields.get(j).get("inferText")).replaceAll("\\s+", "");
            Matcher matcher = bizNumPattern.matcher(nextText);
            if (matcher.find()) return matcher.group().replaceAll("-", "");
        }
        return "";
    }

    private String findNameNear(List<Map<String, Object>> fields, int startIdx) {
        for (int j = startIdx + 1; j < Math.min(fields.size(), startIdx + 5); j++) {
            String next = ((String) fields.get(j).get("inferText")).trim();
            if (next.matches("^[가-힣]{2,3}$")) {
                return next;
            }
        }
        return "";
    }

    private String normalizeDate(String raw) {
        String clean = raw.replaceAll("[^0-9]", "");
        if (clean.length() == 8) return clean;
        if (clean.length() == 6) return clean + "01";
        return clean;
    }
}
