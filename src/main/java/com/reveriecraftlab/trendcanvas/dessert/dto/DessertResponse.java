package com.reveriecraftlab.trendcanvas.dessert.dto;

import com.reveriecraftlab.trendcanvas.domain.Dessert;

/**
 * 홈 API가 내려주는 디저트 한 개. 화면에 필요한 값(번호와 키워드)만 담는다.
 * 엔티티를 그대로 응답으로 내리지 않는 이유: 엔티티에는 created_at처럼 화면에 필요 없는
 * 값이 있고, 나중에 엔티티가 바뀌면 API 응답 모양도 따라 바뀌어 프런트가 깨질 수 있다.
 * 응답 모양을 이 record 하나로 고정해 두면 DB 구조가 바뀌어도 API는 그대로 유지된다.
 */
public record DessertResponse(Long id, String keyword) {

    public static DessertResponse from(Dessert dessert) {
        return new DessertResponse(dessert.getId(), dessert.getKeyword());
    }
}
