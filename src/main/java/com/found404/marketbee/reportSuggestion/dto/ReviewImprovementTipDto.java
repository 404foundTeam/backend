package com.found404.marketbee.reportSuggestion.dto;

import com.found404.marketbee.reviewAnalysis.ReviewAnalysis;
import lombok.Getter;
import java.util.List;

@Getter
public class ReviewImprovementTipDto {
    private final List<String> reviewImprovementTipDto;

    private ReviewImprovementTipDto(List<String> reviewImprovementTipDto) {
        this.reviewImprovementTipDto = reviewImprovementTipDto;
    }

    public static ReviewImprovementTipDto from(ReviewAnalysis reviewAnalysis) {
        List<String> tips = List.of(reviewAnalysis.getImprovementTip1(), reviewAnalysis.getImprovementTip2());
        return new ReviewImprovementTipDto(tips);
    }
}
