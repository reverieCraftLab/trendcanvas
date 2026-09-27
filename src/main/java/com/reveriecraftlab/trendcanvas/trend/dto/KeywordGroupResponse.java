package com.reveriecraftlab.trendcanvas.trend.dto;

import com.reveriecraftlab.trendcanvas.domain.KeywordGroup;

import java.util.List;

/**
 * 프론트 lib/types.ts의 KeywordGroup과 대응.
 * term은 keywordGroup.candidateTerm.term에서 가져온다 — 프론트 입장에서는
 * candidate_term이라는 테이블이 따로 있다는 걸 알 필요가 없으므로 평평하게(flat) 펼쳐서 내려준다.
 */
public record KeywordGroupResponse(
        Long id,
        String term,
        Short rank,
        double relativeRatio,
        List<TrendImageResponse> images
) {
    public static KeywordGroupResponse from(KeywordGroup keywordGroup) {
        List<TrendImageResponse> images = keywordGroup.getImages().stream()
                .map(TrendImageResponse::from)
                .toList();

        return new KeywordGroupResponse(
                keywordGroup.getId(),
                keywordGroup.getCandidateTerm().getTerm(),
                keywordGroup.getRank(),
                keywordGroup.getRelativeRatio().doubleValue(),
                images
        );
    }
}
