package com.found404.marketbee.storeVerify;

import com.found404.marketbee.storeVerify.dto.OcrResponse;
import com.found404.marketbee.storeVerify.dto.VerifyResponse;
import com.found404.marketbee.storeVerify.external.ClovaOcrClient;
import com.found404.marketbee.storeVerify.external.NationalTaxApiClient;
import com.found404.marketbee.storeVerify.external.OcrData;
import com.found404.marketbee.storeVerify.external.OcrResultParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreVerificationService {

    private final ClovaOcrClient clovaOcrClient;
    private final OcrResultParser ocrResultParser;
    private final NationalTaxApiClient nationalTaxApiClient;

    public OcrResponse extractOcr(MultipartFile storeLicense) {
        Map<String, Object> ocrResult = clovaOcrClient.requestOcr(storeLicense);
        OcrData ocrData = ocrResultParser.parse(ocrResult);

        boolean allRecognized =
                isNotBlank(ocrData.storeNumber()) &&
                        isNotBlank(ocrData.representativeName()) &&
                        isNotBlank(ocrData.openDate());

        String message = allRecognized
                ? "OCR 인식 결과입니다. 올바른지 확인 후 수정 가능합니다."
                : "OCR 인식에 실패했습니다. 일부 정보가 누락되었습니다.";

        return new OcrResponse(
                isNotBlank(ocrData.storeNumber()) ? ocrData.storeNumber() : null,
                isNotBlank(ocrData.representativeName()) ? ocrData.representativeName() : null,
                isNotBlank(ocrData.openDate()) ? ocrData.openDate() : null,
                message
        );
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public VerifyResponse verifyWithNts(String storeNumber, String representativeName, String openDate) {
        Map<String, String> ntsResult = nationalTaxApiClient.verify(storeNumber, representativeName, openDate);
        String valid = ntsResult.getOrDefault("valid", "99");
        String msg = ntsResult.getOrDefault("validMsg", "");

        boolean verified;
        String message;

        switch (valid) {
            case "01":
                verified = true;
                message = "사업자 정보가 국세청 등록정보와 일치합니다.";
                break;
            case "02":
                verified = false;
                if ("국세청에 등록되지 않은 사업자등록번호입니다.".equals(msg)) {
                    message = "삭제되었거나 국세청에 존재하지 않는 사업자등록번호입니다.";
                } else {
                    message = "입력한 사업자 정보가 국세청 등록정보와 일치하지 않습니다.";
                }
                break;
            case "03":
                verified = false;
                message = "대표자명 또는 개업일자가 일치하지 않습니다.";
                break;
            case "99":
            default:
                verified = false;
                message = "국세청 시스템 오류로 일시적으로 확인이 불가합니다.";
                break;
        }

        log.info("진위확인 결과: verified={}, message={}", verified, message);
        return new VerifyResponse(verified, message);
    }
}

