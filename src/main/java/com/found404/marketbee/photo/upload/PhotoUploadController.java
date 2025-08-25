package com.found404.marketbee.photo.upload;

import com.found404.marketbee.photo.common.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/v1/photo")
@RequiredArgsConstructor
public class PhotoUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<PhotoUploadResp> upload(@RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        FileStorageService.StoredFile stored = fileStorageService.store(file);

        PhotoUploadResp resp = PhotoUploadResp.builder()
                .fileUrl(stored.getFileUrl())
                .build();

        return ResponseEntity.ok(resp);
    }
}