package com.reveriecraftlab.trendcanvas.image.service;

import com.reveriecraftlab.trendcanvas.naver.client.NaverImageClient;
import com.reveriecraftlab.trendcanvas.naver.dto.NaverImageResponse;

import org.springframework.stereotype.Service;

/**
 * 키워드로 이미지를 찾아 네이버 응답을 그대로 돌려준다.
 *
 * 여기에 @Transactional을 붙이지 않은 이유: 이 서비스는 DB를 쓰지 않는다. 트랜잭션은
 * DB 연결을 잡고 있는 구간을 정하는 도구인데, 외부 API 호출 동안 DB 연결을 붙잡고 있으면
 * 네이버가 느릴 때 연결만 낭비된다. DB가 필요한 일(예: 행동 기록 저장)은 이 호출과 분리해서
 * 짧은 트랜잭션으로 따로 처리한다.
 */
@Service
public class ImageSearchService {

    // 한 번에 받을 이미지 수. 네이버는 display 값과 관계없이 요청 1건을 1회로 세므로(NFR-04 측정)
    // 많이 받을수록 같은 이미지를 보여줄 때 호출이 적게 든다. 처음에는 최대치(100장)로 했으나
    // 한 번에 받는 양을 줄이고 싶다는 GT의 결정으로 10장으로 바꿨다(2026-10-22). 호출은 100장일 때의 10배가 된다.
    // 고정값이라 사용자가 바꿀 수 없게 하고, 한도를 소모하는 입력을 서버가 통제한다.
    // 프런트(trendcanvas-web)의 lib/api.ts IMAGE_PAGE_SIZE와 같은 값이어야 한다.
    static final int DISPLAY = 10;

    // 이미지가 너무 다양하게 들어와서 검색어 뒤에 붙이는 말(2026-10-22 GT 결정). 비우면("") 붙이지 않는다.
    // 검색어는 우리가 만드는 값이라 바꿔 보내는 것은 네이버 결과를 가공하는 것이 아니다(NFR-05과 무관).
    // 서비스 계층에서 붙이는 이유: 행동 기록(FR-08)에는 사용자가 입력한 원래 검색어를 남겨야 해서
    // 컨트롤러가 받은 값은 그대로 두고 네이버로 보내는 순간에만 바꾼다.
    // 주의: 이전 테스트(D-07)에서 "디저트"를 붙이면 뉴스·스톡 사진이 늘고 개선되지 않았다. 효과를 보며 조정한다.
    static final String QUERY_SUFFIX = " 디저트";

    private final NaverImageClient naverImageClient;

    public ImageSearchService(NaverImageClient naverImageClient) {
        this.naverImageClient = naverImageClient;
    }

    public NaverImageResponse search(String keyword, int start) {
        return naverImageClient.search(keyword + QUERY_SUFFIX, start, DISPLAY);
    }
}
