package com.reveriecraftlab.trendcanvas.naver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.yml의 naver.api.* 설정을 한곳에 묶는다.
 *
 * @Value를 여기저기 흩뿌리지 않고 한 타입으로 받으면 "이 서비스가 어떤 설정을 쓰는지"가
 * 이 파일 하나로 드러난다. record라서 시작한 뒤 값이 바뀌지 않는다.
 * 키(clientId, clientSecret)는 코드에 쓰지 않고 환경 변수에서만 받는다(NFR-06).
 */
@ConfigurationProperties(prefix = "naver.api")
public record NaverProperties(String baseUrl, String clientId, String clientSecret) {
}
