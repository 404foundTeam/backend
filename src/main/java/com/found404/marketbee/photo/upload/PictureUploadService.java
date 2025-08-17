package com.found404.marketbee.photo.upload;

import com.found404.marketbee.common.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PictureUploadService {
    private final PictureRepository pictureRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public Picture upload(String storeUuid, MultipartFile file) throws Exception {
        var stored = fileStorageService.store(file);
        Picture pic = Picture.builder()
                .storeUuid(storeUuid)
                .filePath(stored.filePath())
                .fileUrl(stored.fileUrl())
                .originalFilename(stored.originalName())
                .contentType(stored.contentType())
                .build();
        return pictureRepository.save(pic);
    }
}
