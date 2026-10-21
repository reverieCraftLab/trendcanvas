package com.reveriecraftlab.trendcanvas.dessert.repository;

import com.reveriecraftlab.trendcanvas.dessert.domain.Dessert;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DessertRepository extends JpaRepository<Dessert, Long> {

    /**
     * 노출 중인(visible = true) 디저트에서 무작위로 limit개를 뽑는다.
     *
     * native query를 쓴 이유: random()은 PostgreSQL의 함수이고, JPQL에는 표준 무작위 함수가
     * 없다. 이 서비스의 DB는 PostgreSQL로 정해져 있어서(NFR-07) DB 전용 SQL을 써도 이식성
     * 문제가 없고, 쿼리가 눈에 그대로 보이는 쪽이 더 이해하기 쉽다.
     *
     * ORDER BY random()은 행 전체를 섞는 방식이라 행이 수만 개면 느려진다. 이 테이블은
     * 작성자가 직접 넣는 수십 개 수준이라(요구사항 5.1) 문제가 없고, 규모가 커지면 그때 바꾼다.
     */
    @Query(value = "SELECT * FROM dessert WHERE visible = TRUE ORDER BY random() LIMIT :limit",
            nativeQuery = true)
    List<Dessert> findRandomVisible(@Param("limit") int limit);
}
