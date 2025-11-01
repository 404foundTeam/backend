package com.found404.marketbee.storeVerify;

import com.found404.marketbee.storeVerify.dto.VerifyRequest;
import com.found404.marketbee.storeVerify.dto.OcrResponse;
import com.found404.marketbee.storeVerify.dto.VerifyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class StoreVerificationController {

    private final StoreVerificationService storeVerificationService;

    @PostMapping("/verify")
    public ResponseEntity<VerifyResponse> verifyStore(@RequestBody VerifyRequest request) {
        VerifyResponse response = storeVerificationService.verifyWithNts(
                request.storeNumber(), request.representativeName(), request.openDate());
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OcrResponse> extractOcr(@RequestPart("storeLicense") MultipartFile storeLicense) {
        OcrResponse response = storeVerificationService.extractOcr(storeLicense);
        return ResponseEntity.ok(response);
    }
}