package com.reveriecraftlab.trendcanvas.trend.service;

import com.reveriecraftlab.trendcanvas.domain.KeywordGroup;
import com.reveriecraftlab.trendcanvas.trend.dto.TrendResponse;
import com.reveriecraftlab.trendcanvas.trend.repository.KeywordGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 조회 전용 서비스. "오늘의 5개 + 이미지"를 가져와 DTO로 변환하는 일만 한다.
 *
 * @Transactional(readOnly = true)를 붙인 이유:
 * - open-in-view: false 환경이라, 컨트롤러/DTO 변환까지 영속성 컨텍스트(세션)가 살아있게
 *   하려면 이 트랜잭션 범위 "안에서" fetch join 결과와 DTO 변환이 모두 끝나야 한다.
 *   이 메서드가 끝나는 순간 트랜잭션이 커밋/종료되며 영속성 컨텍스트도 닫히므로,
 *   변환이 끝나지 않은 지연 로딩(LAZY) 필드에 컨트롤러에서 접근하면
 *   LazyInitializationException이 난다. 그래서 "엔티티 → DTO 변환"을 이 서비스 메서드
 *   안에서 전부 마치고, 컨트롤러에는 이미 완성된 DTO만 반환한다.
 * - readOnly = true는 조회 전용임을 명시해서, Hibernate가 변경 감지(dirty checking)를
 *   위한 스냅샷을 따로 안 만들게 한다. 쓰기가 없는 트랜잭션이라 약간의 성능 이득도 있다.
 */
@Service
public class TrendQueryService {

    private final KeywordGroupRepository keywordGroupRepository;

    public TrendQueryService(KeywordGroupRepository keywordGroupRepository) {
        this.keywordGroupRepository = keywordGroupRepository;
    }

    @Transactional(readOnly = true)
    public TrendResponse getTodayTrends(String categoryName, LocalDate collectedDate) {
        List<KeywordGroup> groups = keywordGroupRepository.findTodayGroupsWithImages(categoryName, collectedDate);
        return TrendResponse.of(categoryName, collectedDate, groups);
    }
}
