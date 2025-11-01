package com.found404.marketbee.photo.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class PhotoUploadResp {
    private String fileUrl;
}