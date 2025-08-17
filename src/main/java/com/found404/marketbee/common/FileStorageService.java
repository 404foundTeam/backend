package com.found404.marketbee.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileStorageService {
    @Value("${app.file-upload-dir}") private String uploadDir;
    @Value("${app.public-url-prefix}") private String publicPrefix;

    public StoredFile store(MultipartFile file) throws IOException {
        Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(base);

        String ext = "";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }
        String name = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "_" + UUID.randomUUID() + ext;

        Path target = base.resolve(name);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        String url = publicPrefix + "/" + name;

        return new StoredFile(target.toString(), url, original, file.getContentType());
    }

    public record StoredFile(String filePath, String fileUrl, String originalName, String contentType) {}
}
