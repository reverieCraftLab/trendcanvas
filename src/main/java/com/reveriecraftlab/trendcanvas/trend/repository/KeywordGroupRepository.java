package com.reveriecraftlab.trendcanvas.trend.repository;

import com.reveriecraftlab.trendcanvas.domain.KeywordGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface KeywordGroupRepository extends JpaRepository<KeywordGroup, Long> {

    /**
     * "카테고리 이름 + 수집일"로 오늘의 키워드 5개와 그에 딸린 이미지를 한 번에 가져온다.
     *
     * join fetch를 쓰는 이유(N+1 문제):
     * 이 메서드 없이 그냥 findByCategory...() 같은 걸로 keyword_group 5개를 가져오면,
     * 화면에서 group.getImages()를 부르는 순간 5번(그룹당 한 번씩) 추가 쿼리가 나간다.
     * fetch join으로 처음부터 image까지 함께 SELECT하면 쿼리 1번으로 끝난다.
     *
     * candidateTerm도 함께 fetch join한 이유: DTO로 변환할 때 term 문자열(candidateTerm.getTerm())이
     * 필요한데, 여기서 같이 안 가져오면 group마다 또 추가 쿼리가 나간다(N+1이 그대로 재현됨).
     * category는 여기서 join하지 않았다 — 컨트롤러가 category 이름을 파라미터로 이미 알고
     * 있어서, 응답을 만들 때 엔티티의 category를 다시 조회할 필요가 없기 때문이다.
     *
     * DISTINCT를 쓴 이유: keyword_group 1개당 image가 여러 장이라, join한 결과는
     * "keyword_group 행 수 x image 행 수"만큼 로우가 뻥튀기된다. JPQL의 DISTINCT는
     * 이 중복된 KeywordGroup 엔티티 참조를 하나로 합쳐준다(SQL의 DISTINCT처럼 값 전체를
     * 비교하는 게 아니라 엔티티 식별자 기준으로 중복을 제거한다).
     */
    @Query("""
            SELECT DISTINCT kg
            FROM KeywordGroup kg
            JOIN FETCH kg.candidateTerm ct
            LEFT JOIN FETCH kg.images img
            WHERE kg.category.name = :categoryName
              AND kg.collectedDate = :collectedDate
            ORDER BY kg.rank ASC
            """)
    List<KeywordGroup> findTodayGroupsWithImages(
            @Param("categoryName") String categoryName,
            @Param("collectedDate") LocalDate collectedDate
    );
}
