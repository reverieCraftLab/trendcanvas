package com.reveriecraftlab.trendcanvas.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 브라우저가 다른 주소의 프런트(예: localhost:3000)에서 이 API(localhost:8080)를 부를 수 있게 허용한다.
 *
 * CORS가 필요한 이유: 브라우저는 보안상 "페이지가 열린 주소와 다른 주소"로 가는 요청의 응답을
 * 서버가 허락했을 때만 페이지에 넘겨준다. 서버가 아무 허락도 하지 않으면 요청은 가도 브라우저가
 * 응답을 막는다. 이 허락이 CORS 설정이다. (curl이나 서버끼리의 호출에는 적용되지 않는다.)
 *
 * 허용 범위를 좁게 둔 이유: 이 API는 읽기 전용이라 GET만 허용하고, 쿠키 같은 인증 정보는 쓰지 않는다.
 * 허용할 주소는 환경 변수로 받아 로컬(localhost:3000)과 배포 주소를 코드 수정 없이 바꾼다.
 * "*"(모든 주소 허용)는 쓰지 않는다. 아무 사이트나 우리 API를 부르게 되어 네이버 일일 한도(NFR-04)를
 * 다른 사이트가 소모할 수 있기 때문이다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    // 쉼표로 여러 주소를 줄 수 있다(예: 로컬과 배포 주소).
    public WebConfig(@Value("${app.cors.allowed-origins}") String[] allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET");
    }
}
