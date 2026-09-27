package com.reveriecraftlab.trendcanvas.trend.web;

import com.reveriecraftlab.trendcanvas.trend.dto.TrendResponse;
import com.reveriecraftlab.trendcanvas.trend.service.TrendQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * MVP 범위(트렌드 갤러리 조회만)에 맞춘 단일 엔드포인트.
 * 좋아요/검색/로그인은 CLAUDE.md 프로젝트 규칙에 따라 여기에 넣지 않는다.
 */
@RestController
public class TrendController {

    // "오늘"의 기준을 KST로 고정한다. 서버(EC2)는 보통 UTC로 시간을 다루므로,
    // ZoneId 없이 LocalDate.now()를 쓰면 자정 근처에서 서버 기준 날짜와 실제
    // 한국 날짜가 하루 어긋날 수 있다. keyword_group.collected_date 자체가
    // "수집기가 KST로 계산해서 넣은 날짜"이므로, 조회 쪽도 반드시 같은 기준(KST)으로
    // "오늘"을 계산해야 서로 어긋나지 않는다.
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final TrendQueryService trendQueryService;

    public TrendController(TrendQueryService trendQueryService) {
        this.trendQueryService = trendQueryService;
    }

    /**
     * GET /api/trends?category=디저트
     *
     * date 파라미터는 필수로 받지 않는다. 기본값은 "오늘(KST)"이고,
     * 특정 날짜를 확인하고 싶을 때(예: 트러블슈팅)만 선택적으로 넘길 수 있게 열어둔다.
     */
    @GetMapping("/api/trends")
    public TrendResponse getTodayTrends(
            @RequestParam String category,
            @RequestParam(required = false) LocalDate date
    ) {
        LocalDate collectedDate = date != null ? date : LocalDate.now(KST);
        return trendQueryService.getTodayTrends(category, collectedDate);
    }
}
