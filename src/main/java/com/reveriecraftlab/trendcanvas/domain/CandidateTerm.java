package com.reveriecraftlab.trendcanvas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * 후보 키워드 원본. "마카롱" 같은 term 문자열을 한 번만 저장하고
 * keyword_group이 이 엔티티를 참조하는 구조다.
 *
 * V1__init_schema.sql의 candidate_term 테이블과 대응.
 */
@Entity
@Table(name = "candidate_term")
public class CandidateTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // fetch = LAZY: candidate_term을 조회할 때마다 category까지 매번 join해서 가져올 필요는 없다.
    // open-in-view: false 환경이라, category가 실제로 필요한 화면/로직에서만
    // 명시적으로(fetch join 등으로) 함께 가져오는 게 원칙이다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "term", nullable = false, length = 100)
    private String term;

    protected CandidateTerm() {
    }

    public CandidateTerm(Category category, String term) {
        this.category = category;
        this.term = term;
    }

    public Long getId() {
        return id;
    }

    public Category getCategory() {
        return category;
    }

    public String getTerm() {
        return term;
    }
}
