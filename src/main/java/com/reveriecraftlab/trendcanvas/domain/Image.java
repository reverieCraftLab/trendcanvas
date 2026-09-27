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

import java.time.OffsetDateTime;

/**
 * keyword_group 하나에 딸린 이미지 한 장.
 *
 * V1__init_schema.sql의 image 테이블과 대응.
 * keyword_group_id 외래키를 실제로 들고 있는 쪽이라, 연관관계의 주인이다
 * (KeywordGroup.images는 mappedBy로 이 필드를 참조만 한다).
 */
@Entity
@Table(name = "image")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_group_id", nullable = false)
    private KeywordGroup keywordGroup;

    @Column(name = "source", nullable = false, length = 30)
    private String source;

    @Column(name = "source_id", length = 100)
    private String sourceId;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "source_page_url", length = 500)
    private String sourcePageUrl;

    // width/height: 프론트 매소너리 그리드가 이미지 로딩 전에 aspect-ratio를 계산하려고
    // 추가한 컬럼(V1 마이그레이션 주석 참고). SMALLINT라 Java에서는 Short로 받는다.
    @Column(name = "width", nullable = false)
    private Short width;

    @Column(name = "height", nullable = false)
    private Short height;

    // KeywordGroup.createdAt과 같은 이유: DB의 DEFAULT now()를 읽기만 하고 애플리케이션이 쓰지 않는다.
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected Image() {
    }

    public Image(KeywordGroup keywordGroup, String source, String sourceId, String imageUrl,
                 String sourcePageUrl, Short width, Short height) {
        this.keywordGroup = keywordGroup;
        this.source = source;
        this.sourceId = sourceId;
        this.imageUrl = imageUrl;
        this.sourcePageUrl = sourcePageUrl;
        this.width = width;
        this.height = height;
    }

    public Long getId() {
        return id;
    }

    public KeywordGroup getKeywordGroup() {
        return keywordGroup;
    }

    public String getSource() {
        return source;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSourcePageUrl() {
        return sourcePageUrl;
    }

    public Short getWidth() {
        return width;
    }

    public Short getHeight() {
        return height;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
