package com.reveriecraftlab.trendcanvas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 대분류(카테고리) 마스터. 지금은 "디저트" 한 행뿐이다.
 *
 * V1__init_schema.sql의 category 테이블과 1:1로 대응한다.
 * candidate_term, keyword_group이 이 테이블을 FK로 참조하므로
 * 연관관계의 "부모" 쪽에 해당한다.
 */
@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // BIGSERIAL이라 DB가 값을 채번한다. IDENTITY 전략이 그 방식과 맞는다.
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    protected Category() {
        // JPA가 프록시/리플렉션으로 엔티티를 만들 때 쓰는 기본 생성자.
        // 외부에서 new Category()로 빈 객체를 만들 수 없게 protected로 막는다.
    }

    public Category(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
