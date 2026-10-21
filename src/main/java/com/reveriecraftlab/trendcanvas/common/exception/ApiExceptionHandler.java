package com.reveriecraftlab.trendcanvas.common.exception;

import com.reveriecraftlab.trendcanvas.naver.exception.NaverQuotaExceededException;
import com.reveriecraftlab.trendcanvas.naver.exception.NaverUpstreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 네이버 호출 실패를 프런트가 구분할 수 있는 응답으로 바꾼다(NFR-04, NFR-09).
 *
 * 한도 초과와 일반 실패를 코드(code)로 나누는 이유: 프런트가 "오늘은 이미지를 더 불러올 수 없어요"와
 * "잠시 후 다시 시도해 주세요"처럼 다른 안내를 보여줄 수 있게 하기 위해서다.
 * 오류 본문에는 네이버의 응답이나 키를 넣지 않는다.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    public record ErrorResponse(String code, String message) {
    }

    @ExceptionHandler(NaverQuotaExceededException.class)
    public ResponseEntity<ErrorResponse> handleQuota(NaverQuotaExceededException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("QUOTA_EXCEEDED", e.getMessage()));
    }

    @ExceptionHandler(NaverUpstreamException.class)
    public ResponseEntity<ErrorResponse> handleUpstream(NaverUpstreamException e) {
        // 사용자에게는 원인을 알리지 않지만, 개발자가 원인을 찾을 수 있도록 로그에는 남긴다.
        // 로그에도 키나 네이버 응답 본문은 넣지 않는다(예외 메시지와 원인 예외만 기록).
        log.warn("네이버 호출 실패: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse("UPSTREAM_ERROR", "이미지를 불러오지 못했습니다."));
    }

    // 키가 설정되지 않은 것 같은 서버 설정 오류. 처리하지 않으면 Spring이 원인 없이 500만 돌려줘서
    // 이번처럼 찾기 어려우므로, 로그에 원인을 남기고 일반 500으로 응답한다.
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleServerConfig(IllegalStateException e) {
        log.error("서버 설정 오류: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("SERVER_ERROR", "서버에 문제가 발생했습니다."));
    }
}
