package com.found404.marketbee.photo.analysis;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DataUrlReq {
    private String dataUrl;     // "data:image/png;base64,..." 형태로 받는 경우
}
