package com.reveriecraftlab.trendcanvas.trend.dto;

import com.reveriecraftlab.trendcanvas.domain.KeywordGroup;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * 프론트 lib/types.ts의 TrendResponse와 최상위 응답 모양을 그대로 맞춘 DTO.
 * 이 모양을 프론트와 똑같이 맞춰두면, lib/trends.ts의 getTrends()를
 * mock.json 읽기에서 fetch(API 호출)로 바꾸기만 하면 되고 나머지 화면 코드는
 * 건드릴 필요가 없다.
 */
public record TrendResponse(
        String category,
        LocalDate collectedAt,
        List<KeywordGroupResponse> keywordGroups
) {
    public static TrendResponse of(String category, LocalDate collectedDate, List<KeywordGroup> groups) {
        List<KeywordGroupResponse> keywordGroups = groups.stream()
                .sorted(Comparator.comparing(KeywordGroup::getRank))
                .map(KeywordGroupResponse::from)
                .toList();

        return new TrendResponse(category, collectedDate, keywordGroups);
    }
}
