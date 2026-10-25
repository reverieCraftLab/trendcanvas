package com.reveriecraftlab.trendcanvas.dessert.service;

import com.reveriecraftlab.trendcanvas.dessert.dto.DessertResponse;
import com.reveriecraftlab.trendcanvas.dessert.repository.DessertRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 홈에 보여줄 디저트를 고르는 조회 전용 서비스.
 *
 * @Transactional(readOnly = true)를 붙인 이유:
 * - 이 메서드는 DB를 읽기만 한다. 읽기 전용으로 선언하면 Hibernate가 변경 감지용 스냅샷을
 *   만들지 않아 조금 가볍고, "여기서는 아무것도 바꾸지 않는다"는 의도가 코드에 남는다.
 * - 엔티티를 DTO로 바꾸는 일까지 이 트랜잭션 안에서 끝낸다. open-in-view를 꺼 두었기 때문에
 *   메서드가 끝나면 영속성 컨텍스트도 닫힌다. 컨트롤러에는 이미 완성된 DTO만 내려준다.
 */
@Service
public class DessertQueryService {

    // 홈에 한 번에 보여줄 디저트 수. 프런트의 ㄱ자 묶음이 디저트 한 개당 한 겹을 쓰고, 겹 수는 화면의 가로 칸 수(최대 10)다.
    // 5개에서 10개로 늘렸다(2026-10-22 GT 결정, FR-01). 노출 중인 디저트가 이보다 적으면 있는 만큼만 나간다.
    static final int HOME_LIMIT = 10;

    private final DessertRepository dessertRepository;

    public DessertQueryService(DessertRepository dessertRepository) {
        this.dessertRepository = dessertRepository;
    }

    @Transactional(readOnly = true)
    public List<DessertResponse> getHomeDesserts() {
        return dessertRepository.findRandomVisible(HOME_LIMIT).stream()
                .map(DessertResponse::from)
                .toList();
    }
}
