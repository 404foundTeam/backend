package com.found404.marketbee.sns.enums;

import lombok.Getter;

@Getter
public enum CardRatio {
    SQUARE_1_1(1024, 1024),
    RATIO_2_3(1024, 1536), //세로
    RATIO_3_2(1536, 1024); //가로

    private final int width;
    private final int height;

    CardRatio(int width, int height) {
        this.width = width;
        this.height = height;
    }
}