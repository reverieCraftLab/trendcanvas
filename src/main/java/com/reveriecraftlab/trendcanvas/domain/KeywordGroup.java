package com.reveriecraftlab.trendcanvas.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * "특정 날짜, 특정 카테고리에서 몇 위로 뽑힌 키워드"라는 사실.
 * "오늘의 5개" 조회는 결국 이 테이블을 (category_id, collected_date)로 훑는 것과 같다.
 *
 * V1__init_schema.sql의 keyword_group 테이블과 대응.
 *
 * fetch 전략에 대한 메모(이후 리포지토리/API 설계에서 중요):
 * - category, candidateTerm은 LAZY로 뒀다. 조회 API는 category_id를 이미 알고 요청하므로
 *   category 엔티티 자체를 화면에 다시 내려줄 필요가 없는 경우가 많다. 필요할 때만
 *   fetch join으로 명시적으로 가져온다.
 * - images는 OneToMany인데, 이건 십중팔구 화면에 항상 같이 필요하다(카드 하나 = keyword_group 1개
 *   + image 여러 장). 그래도 기본은 LAZY로 두고, 조회 쿼리 쪽(Repository의 @Query)에서
 *   fetch join으로 "이번 조회에서만" 함께 가져오는 방식을 쓸 것이다. 연관관계 어노테이션에
 *   fetch = EAGER를 박아버리면 이 엔티티를 쓰는 모든 곳에서 항상 image까지 join되어,
 *   당장은 편해도 나중에 다른 용도로 재사용할 때 불필요한 join이 강제된다.
 */
@Entity
@Table(name = "keyword_group")
public class KeywordGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id", nullable = false)
    private CandidateTerm candidateTerm;

    @Column(name = "collected_date", nullable = false)
    private LocalDate collectedDate;

    @Column(name = "rank", nullable = false)
    private Short rank;

    @Column(name = "relative_ratio", nullable = false)
    private BigDecimal relativeRatio;

    // mappedBy = "keywordGroup": 외래키(keyword_group_id)의 주인은 Image 쪽이다.
    // KeywordGroup은 그 관계를 "읽기 전용"으로 들고 있을 뿐, 이 리스트를 통해 DB에
    // 새 FK 컬럼을 만들거나 변경하지 않는다. (연관관계의 주인을 Image로 둔 이유는
    // 실제 FK 컬럼이 image 테이블에 있기 때문 — 매핑을 테이블 구조 그대로 따라간다)
    //
    // cascade = ALL, orphanRemoval = true: keyword_group 하나를 지우면 그에 딸린 image들도
    // 같이 정리되어야 자연스럽다(고아 이미지가 남으면 안 됨). DB 레벨에서도
    // ON DELETE CASCADE를 걸어뒀지만, 애플리케이션에서 keywordGroup.getImages().remove(...)로
    // 리스트에서 이미지를 뺐을 때도 실제로 삭제되게 하려면 orphanRemoval이 필요하다.
    @OneToMany(mappedBy = "keywordGroup", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<Image> images = new ArrayList<>();

    // DB의 DEFAULT now()가 실제 값을 채운다. insertable/updatable을 둘 다 false로 두면
    // Hibernate가 INSERT/UPDATE 쿼리에 이 컬럼을 아예 포함시키지 않고, SELECT할 때만
    // DB에 있는 값을 읽어온다. 이 값을 자바 쪽에서 세팅할 생성자/세터를 안 만든 이유:
    // created_at은 "언제 이 행이 생겼는가"라는 사실이라 애플리케이션이 임의로 바꿀 수
    // 있으면 안 되는 값이기 때문이다.
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected KeywordGroup() {
    }

    public KeywordGroup(Category category, CandidateTerm candidateTerm, LocalDate collectedDate,
                         Short rank, BigDecimal relativeRatio) {
        this.category = category;
        this.candidateTerm = candidateTerm;
        this.collectedDate = collectedDate;
        this.rank = rank;
        this.relativeRatio = relativeRatio;
    }

    public Long getId() {
        return id;
    }

    public Category getCategory() {
        return category;
    }

    public CandidateTerm getCandidateTerm() {
        return candidateTerm;
    }

    public LocalDate getCollectedDate() {
        return collectedDate;
    }

    public Short getRank() {
        return rank;
    }

    public BigDecimal getRelativeRatio() {
        return relativeRatio;
    }

    public List<Image> getImages() {
        return images;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
