package com.reveriecraftlab.trendcanvas.naver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * 네이버 이미지 검색 응답. 값을 가공하지 않고 그대로 전달한다(NFR-05, 약관 특약 1.2~1.3).
 *
 * - sizeheight, sizewidth는 네이버가 문자열로 주므로 문자열로 받는다. 숫자로 바꾸는 것도
 *   가공이라 서버에서는 하지 않고, 필요하면 프런트에서 변환한다.
 * - title에는 검색어를 감싼 HTML 태그(<b>)가 섞여 있을 수 있다. 서버가 지우지 않으므로
 *   프런트는 이 값을 HTML로 해석하지 말고 반드시 글자로만 표시해야 한다(XSS 방지).
 * - ignoreUnknown: 네이버가 필드를 더 보내도 오류 없이 무시한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverImageResponse(int total, int start, int display, List<Item> items) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(String title, String link, String thumbnail,
                       String sizeheight, String sizewidth) {
    }
}
