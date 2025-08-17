package com.found404.marketbee.photo.upload;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity @Table(name = "pictures")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Picture {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="store_uuid", length=36, nullable=false)
    private String storeUuid;

    @Column(name="file_path", length=255, nullable=false)
    private String filePath;

    @Column(name="file_url", length=255, nullable=false)
    private String fileUrl;

    @Column(name="original_filename", length=255)
    private String originalFilename;

    @Column(name="content_type", length=100)
    private String contentType;

    @CreationTimestamp
    @Column(name="created_at", updatable=false)
    private LocalDateTime createdAt;
}
