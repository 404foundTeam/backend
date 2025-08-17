package com.found404.marketbee.photo.upload;

import lombok.Builder;

@Builder
public record UploadResponse(Long pictureId, String storeUuid, String fileUrl, String message) {
    public static UploadResponse of(Picture p) {
        return UploadResponse.builder()
                .pictureId(p.getId())
                .storeUuid(p.getStoreUuid())
                .fileUrl(p.getFileUrl())
                .message("업로드 완료")
                .build();
    }
}
