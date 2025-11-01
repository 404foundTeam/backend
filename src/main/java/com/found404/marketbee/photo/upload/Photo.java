package com.found404.marketbee.photo.upload;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "photos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String storeUuid;
    private String pictureId;

    private String originalFilename;
    private String storedFilename;

    private String fileUrl;

    @Column(length = 2000)
    private String guideText;

    @CreationTimestamp
    private LocalDateTime createdAt;

}
