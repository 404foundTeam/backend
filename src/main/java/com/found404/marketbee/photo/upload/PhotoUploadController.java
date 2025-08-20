package com.found404.marketbee.photo.upload;

import com.found404.marketbee.common.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/photo")
@RequiredArgsConstructor
public class PhotoUploadController {

    private final FileStorageService fileStorageService;
    private final PhotoRepository photoRepository;

    @PostMapping("/upload")
    @Transactional
    public ResponseEntity<?> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "storeUuid", required = false) String storeUuid,
            @RequestParam(value = "pictureId", required = false) String pictureId,
            @RequestParam(value = "guideText", required = false) String guideText
    ) {
        if (file.isEmpty()) return ResponseEntity.badRequest().body("file is required");

        FileStorageService.StoredFile stored = fileStorageService.store(file);

        Photo photo = Photo.builder()
                .storeUuid(storeUuid)
                .pictureId(StringUtils.hasText(pictureId) ? pictureId : null)
                .originalFilename(file.getOriginalFilename())
                .storedFilename(stored.getStoredFilename())
                .fileUrl(stored.getFileUrl())
                .guideText(guideText)
                .build();

        photo = photoRepository.save(photo);

        HashMap<String, Object> res = new HashMap<>();
        res.put("photoId", photo.getId());
        res.put("fileUrl", photo.getFileUrl());
        res.put("guideText", photo.getGuideText());

        return ResponseEntity.created(URI.create("/api/v1/photo/" + photo.getId()))
                .body(res);
    }
}
