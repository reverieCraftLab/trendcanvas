package com.reveriecraftlab.trendcanvas.naver.exception;

/**
 * 네이버가 429(일일 한도 초과)를 줬을 때.
 * 한도 초과는 재시도해도 성공하지 않고 사용자에게 보여줄 안내도 일반 실패와 달라서
 * (NFR-04, NFR-09) 별도 타입으로 구분한다.
 */
public class NaverQuotaExceededException extends RuntimeException {

    public NaverQuotaExceededException() {
        super("네이버 검색 API의 일일 한도를 초과했습니다.");
    }
}
