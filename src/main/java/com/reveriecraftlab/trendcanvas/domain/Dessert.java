package com.reveriecraftlab.trendcanvas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * 홈에 노출할 디저트 한 개. V2__replace_with_dessert.sql의 dessert 테이블과 대응.
 *
 * 이 엔티티에는 setter를 만들지 않았다. 디저트는 작성자가 DB에 직접 넣고(V3 시드, 이후 SQL),
 * 이 서비스의 코드는 읽기만 하기 때문이다. 값을 바꾸는 코드가 없으니 바꿀 수 있는 통로도
 * 열어 두지 않는 것이 실수를 줄인다.
 */
@Entity
@Table(name = "dessert")
public class Dessert {

    // IDENTITY: PostgreSQL의 BIGSERIAL(자동 증가)과 맞는 전략이다. 번호를 DB가 붙이므로
    // 애플리케이션이 번호를 미리 알 수 없고, 저장할 때 바로 INSERT가 실행된다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 화면에 보이면서 네이버에도 보내는 이름(결정 기록 D-08). 길이와 UNIQUE는 DB가 지킨다.
    @Column(name = "keyword", nullable = false, length = 100)
    private String keyword;

    // 행을 지우지 않고 홈에서만 빼는 스위치.
    @Column(name = "visible", nullable = false)
    private boolean visible;

    // DB의 DEFAULT now()가 채운다. insertable/updatable = false로 두면 Hibernate가 INSERT/UPDATE에
    // 이 컬럼을 넣지 않고 읽기만 한다. 등록 시각은 "언제 생겼는가"라는 사실이라
    // 애플리케이션이 임의로 바꾸면 안 되는 값이다.
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    // JPA가 객체를 만들 때 쓰는 기본 생성자. protected로 막아 코드에서 빈 객체를 만들지 못하게 한다.
    protected Dessert() {
    }

    public Long getId() {
        return id;
    }

    public String getKeyword() {
        return keyword;
    }

    public boolean isVisible() {
        return visible;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
