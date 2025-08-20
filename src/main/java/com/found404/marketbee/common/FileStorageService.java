package com.found404.marketbee.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final String ROOT_DIR = "uploads";

    @Data
    @AllArgsConstructor
    public static class StoredFile {
        private String storedFilename;
        private String fileUrl;
    }

    public StoredFile store(MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("empty file");

        LocalDate today = LocalDate.now();
        File dir = new File(ROOT_DIR + File.separator +
                today.getYear() + File.separator +
                String.format("%02d", today.getMonthValue()) + File.separator +
                String.format("%02d", today.getDayOfMonth()));
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("cannot create upload dir: " + dir.getAbsolutePath());
        }

        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String storedName = UUID.randomUUID() + (ext != null ? "." + ext : "");
        File dest = new File(dir, storedName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String relativePath = dir.getPath().replace(ROOT_DIR + File.separator, "") + File.separator + storedName;
        String url = "/uploads/" + relativePath.replace(File.separatorChar, '/');
        return new StoredFile(storedName, url);
    }
}
