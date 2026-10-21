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
    // 한 번에 최대치(100장)를 받는 것이 같은 이미지를 보여줄 때 호출이 가장 적게 든다.
    // 고정값이라 사용자가 바꿀 수 없게 하고, 한도를 소모하는 입력을 서버가 통제한다.
    static final int DISPLAY = 100;

    private final NaverImageClient naverImageClient;

    public ImageSearchService(NaverImageClient naverImageClient) {
        this.naverImageClient = naverImageClient;
    }

    public NaverImageResponse search(String keyword, int start) {
        return naverImageClient.search(keyword, start, DISPLAY);
    }
}
