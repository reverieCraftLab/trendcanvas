-- ============================================================
-- V1: 초기 스키마
--
-- 이 파일 하나로 시작하는 이유:
-- 이전에 "V2"로 부르던 collected_date / image.width,height 논의는
-- 실제로는 아직 이 프로젝트에 마이그레이션이 하나도 없는 상태에서 나온 것이었다.
-- 즉 지금 확정된 내용을 담아 처음부터 하나의 완성된 V1로 만드는 게 맞다.
-- (나중에 실제로 컬럼을 추가/변경해야 할 때가 V2, V3, ... 이다)
-- ============================================================

-- 대분류. 지금은 "디저트" 하나뿐이지만 문자열을 여기저기 하드코딩하지 않고
-- 테이블로 빼두면 나중에 다른 카테고리(예: "커피")를 추가해도 스키마 변경이 필요 없다.
CREATE TABLE category (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- 수집기가 관측한 후보 키워드 원본.
-- "마카롱"이라는 term 자체는 여러 날 반복해서 등장하므로 term 문자열을 매번
-- 새로 만들지 않고 한 번만 저장해서 keyword_group이 참조하게 한다.
-- (term을 keyword_group에 직접 문자열로 박아넣지 않는 이유: 같은 단어가 여러 날
--  반복돼도 오타/띄어쓰기 차이 없이 항상 같은 term_id를 가리키게 하기 위함)
CREATE TABLE candidate_term (
    id         BIGSERIAL PRIMARY KEY,
    term       VARCHAR(100) NOT NULL,
    category_id BIGINT NOT NULL REFERENCES category(id),
    CONSTRAINT uk_candidate_term_category_term UNIQUE (category_id, term)
);

-- "특정 날짜, 특정 카테고리에서 몇 위로 뽑힌 키워드"라는 사실 자체.
-- GT님과 확정한 내용:
--   - "오늘의 5개" 기준은 collected_date(날짜) 컬럼. 별도 회차/세션 테이블을 두지 않는다.
--   - collected_date는 KST 기준 날짜. 수집기(Python, Ubuntu 로컬 크론)가 KST로 계산해서
--     값 그대로 내려준다. DB/서버가 UTC여도 이 컬럼 자체는 타임존 변환의 영향을 받지 않는다.
--     (그래서 TIMESTAMP가 아니라 DATE로 잡았다 — "몇 시 몇 분"이 아니라 "그날"이 기준이므로)
--   - 같은 날 재수집이 들어오면 새 행을 쌓지 않고 덮어쓴다. 그래서 unique 제약을 걸어
--     upsert(INSERT ... ON CONFLICT ... DO UPDATE)가 가능하게 한다.
CREATE TABLE keyword_group (
    id             BIGSERIAL PRIMARY KEY,
    category_id    BIGINT NOT NULL REFERENCES category(id),
    term_id        BIGINT NOT NULL REFERENCES candidate_term(id),
    collected_date DATE NOT NULL,
    rank           SMALLINT NOT NULL,
    relative_ratio NUMERIC(5, 2) NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- 같은 날 같은 카테고리에 같은 순위가 두 번 나올 수 없다.
    -- 이 제약이 "재실행 시 덮어쓰기"를 DB 레벨에서 강제한다: 수집기가 재실행되면
    -- 애플리케이션이 이 키로 upsert하고, 실수로 같은 행을 두 번 INSERT하면 DB가 막아준다.
    CONSTRAINT uk_keyword_group_category_date_rank UNIQUE (category_id, collected_date, rank)
);

-- 조회 쿼리는 항상 "이 카테고리의, 오늘 날짜인 것들"을 찾는 형태이므로
-- (category_id, collected_date)로 시작하는 복합 인덱스를 하나 둔다.
-- 위 UNIQUE 제약이 이미 이 두 컬럼을 포함하는 인덱스를 만들어주지만
-- rank까지 포함된 3컬럼 인덱스라, 굳이 rank 없이 카테고리+날짜로만 훑는 조회는
-- 이 인덱스가 더 잘 맞는다.
CREATE INDEX idx_keyword_group_category_date ON keyword_group (category_id, collected_date);

-- keyword_group 하나에 딸린 이미지 여러 장.
-- width/height: 원래 스키마에는 없었는데 프론트엔드 매소너리 그리드가
-- "이미지가 로딩되기 전에 세로/가로 비율을 미리 알아야 카드 높이를 잡을 수 있어서"
-- 필요해진 컬럼이다. 프론트 lib/types.ts에도 같은 이유로 주석이 남아 있었다.
-- (실제 픽셀 크기가 아니라 aspect-ratio 계산용 비율이라 SMALLINT로 충분하다)
CREATE TABLE image (
    id               BIGSERIAL PRIMARY KEY,
    keyword_group_id BIGINT NOT NULL REFERENCES keyword_group(id) ON DELETE CASCADE,
    source           VARCHAR(30) NOT NULL,
    source_id        VARCHAR(100),
    image_url        VARCHAR(500) NOT NULL,
    source_page_url  VARCHAR(500),
    width            SMALLINT NOT NULL,
    height           SMALLINT NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 조회는 항상 keyword_group_id로 이미지들을 묶어서 가져오므로 FK 컬럼에 인덱스를 건다.
-- (PostgreSQL은 FK를 만들어도 인덱스를 자동으로 만들어주지 않는다 — MySQL과 다른 점)
CREATE INDEX idx_image_keyword_group_id ON image (keyword_group_id);
