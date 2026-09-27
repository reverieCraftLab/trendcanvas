package com.reveriecraftlab.trendcanvas.trend.dto;

import com.reveriecraftlab.trendcanvas.domain.Image;

/**
 * 프론트(trendcanvas-web)의 lib/types.ts TrendImage와 1:1로 대응.
 * record를 쓴 이유: 이 객체는 "조회한 값을 그대로 담아 내려주는" 응답 전용 객체라
 * 값이 바뀔 일이 없다(불변). 클래스 + getter를 직접 쓰는 것보다 record가 그 의도를
 * 코드로 더 명확히 드러낸다.
 */
public record TrendImageResponse(
        Long id,
        String source,
        String sourceId,
        String imageUrl,
        String sourcePageUrl,
        Short width,
        Short height
) {
    public static TrendImageResponse from(Image image) {
        return new TrendImageResponse(
                image.getId(),
                image.getSource(),
                image.getSourceId(),
                image.getImageUrl(),
                image.getSourcePageUrl(),
                image.getWidth(),
                image.getHeight()
        );
    }
}
