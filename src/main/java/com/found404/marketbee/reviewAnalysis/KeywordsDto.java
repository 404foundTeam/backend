package com.found404.marketbee.reviewAnalysis;

import lombok.Getter;
import java.util.List;

@Getter
public class KeywordsDto {
    private final List<String> keywords;

    private KeywordsDto(List<String> keywords) {
        this.keywords = keywords;
    }

    public static KeywordsDto from(ReviewAnalysis reviewAnalysis) {
        List<String> keywords = List.of(
                reviewAnalysis.getKeyword1(),
                reviewAnalysis.getKeyword2(),
                reviewAnalysis.getKeyword3()
        );
        return new KeywordsDto(keywords);
    }
}
