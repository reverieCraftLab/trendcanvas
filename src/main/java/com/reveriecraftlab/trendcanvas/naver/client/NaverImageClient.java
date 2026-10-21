package com.reveriecraftlab.trendcanvas.naver.client;

import com.reveriecraftlab.trendcanvas.naver.config.NaverProperties;
import com.reveriecraftlab.trendcanvas.naver.dto.NaverImageResponse;
import com.reveriecraftlab.trendcanvas.naver.exception.NaverQuotaExceededException;
import com.reveriecraftlab.trendcanvas.naver.exception.NaverUpstreamException;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.time.Duration;

/**
 * 네이버 이미지 검색 API를 호출한다.
 *
 * - 키는 생성할 때 기본 헤더로 한 번만 등록한다. 호출마다 키를 다루지 않고 코드에도 쓰지 않는다(NFR-06).
 * - 429는 재시도하지 않고 예외로 올린다. 한도가 찬 상태의 재시도는 성공하지 않고
 *   오히려 호출만 늘린다(NFR-04, 약관 7.3⑫).
 * - 연결·응답 시간 제한을 둔다. 제한이 없으면 네이버가 느릴 때 요청이 계속 쌓여
 *   t2.micro(메모리 1GB)의 스레드가 묶일 수 있다.
 */
@Component
public class NaverImageClient {

    private static final String SEARCH_PATH = "/search/v1/image";

    // 스레드에 안전해서 하나를 재사용한다. Spring Boot 4는 Jackson 3(tools.jackson)을 쓴다.
    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    private final RestClient restClient;
    private final NaverProperties properties;

    public NaverImageClient(NaverProperties properties) {
        this.properties = properties;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(2));
        requestFactory.setReadTimeout(Duration.ofSeconds(3));

        // RestClient.Builder를 주입받지 않고 직접 만든다. Spring Boot 4에서는 이 빈이 기본으로
        // 만들어지지 않아 주입이 실패했다. 이 클라이언트는 시간 제한과 헤더를 자체 코드로 정하므로
        // Boot의 자동 설정에 기대지 않아도 된다.
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader("X-NCP-APIGW-API-KEY-ID", nullToEmpty(properties.clientId()))
                .defaultHeader("X-NCP-APIGW-API-KEY", nullToEmpty(properties.clientSecret()))
                .build();
    }

    public NaverImageResponse search(String query, int start, int display) {
        // 키가 없으면 앱은 켜지되(DB만 확인할 때 키가 필요 없도록), 호출 시점에 분명한 오류로 알린다.
        if (!StringUtils.hasText(properties.clientId()) || !StringUtils.hasText(properties.clientSecret())) {
            throw new IllegalStateException("NAVER_CLIENT_ID / NAVER_CLIENT_SECRET 환경 변수가 설정되지 않았습니다.");
        }

        // {query} 자리에 값을 따로 넣고 encode()로 값을 엄격하게 인코딩한다.
        // 검색어에 &나 공백이 있어도 다른 파라미터로 오해되지 않게 하기 위해서다.
        URI uri = UriComponentsBuilder.fromUriString(properties.baseUrl())
                .path(SEARCH_PATH)
                .queryParam("query", "{query}")
                .queryParam("display", display)
                .queryParam("start", start)
                .encode()
                .build(query);

        // 응답을 먼저 문자열로 받는다. 네이버(API HUB)가 JSON 내용을 Content-Type: text/plain으로
        // 보내서, 바로 NaverImageResponse로 받으면 "변환기를 찾을 수 없다"는 오류가 났다.
        // Spring의 JSON 변환기는 application/json일 때만 동작하기 때문이다.
        final String body;
        try {
            body = restClient.get()
                    .uri(uri)
                    .retrieve()
                    // 429를 일반 오류보다 먼저 검사해야 따로 처리된다(위에서부터 차례로 맞춘다).
                    .onStatus(status -> status.value() == 429, (req, res) -> {
                        throw new NaverQuotaExceededException();
                    })
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new NaverUpstreamException("네이버 검색 API 호출 실패. status=" + res.getStatusCode().value());
                    })
                    .body(String.class);
        } catch (ResourceAccessException e) {
            // 연결 실패나 시간 초과. 네이버 쪽 실패로 같은 방식으로 다룬다.
            throw new NaverUpstreamException("네이버 검색 API에 연결하지 못했습니다.", e);
        }

        if (!StringUtils.hasText(body)) {
            throw new NaverUpstreamException("네이버 검색 API가 빈 응답을 보냈습니다.");
        }

        // 그리고 우리가 JSON으로 읽는다. 값은 가공하지 않고 필드에 그대로 담는다(NFR-05).
        try {
            return JSON_MAPPER.readValue(body, NaverImageResponse.class);
        } catch (JacksonException e) {
            throw new NaverUpstreamException("네이버 검색 API 응답을 해석하지 못했습니다.", e);
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
