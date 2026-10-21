package com.reveriecraftlab.trendcanvas.image.controller;

import com.reveriecraftlab.trendcanvas.image.service.ImageSearchService;
import com.reveriecraftlab.trendcanvas.naver.dto.NaverImageResponse;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImageController {

    // 네이버 이미지 검색이 받는 start의 최대값으로 알려진 1000이다(네이버 공식 문서 기준, 이 서비스에서
    // 직접 호출해 한계를 확인하지는 않았다). 범위를 넘는 값은 네이버로 보내지 않고 여기서 막는다.
    private static final int MAX_START = 1000;

    private final ImageSearchService imageSearchService;

    public ImageController(ImageSearchService imageSearchService) {
        this.imageSearchService = imageSearchService;
    }

    /**
     * GET /api/images?keyword=두쫀쿠&start=1
     *
     * 홈의 디저트 구역마다 이 API를 따로 부르고, 스크롤로 더 불러올 때는 start만 바꿔 다시 부른다.
     * 사용자 검색(FR-03)도 같은 API를 쓴다.
     *
     * 검증을 컨트롤러에서 먼저 하는 이유: 네이버는 잘못된 요청도 호출 1회로 세므로(NFR-04)
     * 틀린 값이 네이버까지 가면 우리 일일 한도만 줄어든다. 검증 실패는 Spring이 400으로 응답한다.
     * 검색어 길이를 100자로 제한한 것은 dessert.keyword의 길이와 맞춘 값이다.
     */
    @GetMapping("/api/images")
    public NaverImageResponse search(
            @RequestParam @NotBlank @Size(max = 100) String keyword,
            @RequestParam(defaultValue = "1") @Min(1) @Max(MAX_START) int start
    ) {
        return imageSearchService.search(keyword, start);
    }
}
