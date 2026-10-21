package com.reveriecraftlab.trendcanvas.naver.exception;

/** 한도 초과 외에 네이버 호출이 실패했을 때(네이버 오류 응답, 연결 실패, 시간 초과 등). */
public class NaverUpstreamException extends RuntimeException {

    public NaverUpstreamException(String message) {
        super(message);
    }

    public NaverUpstreamException(String message, Throwable cause) {
        super(message, cause);
    }
}
