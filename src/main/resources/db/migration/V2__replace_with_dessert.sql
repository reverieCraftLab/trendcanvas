-- ============================================================
-- V2: 이전 설계(수집·집계 파이프라인) 테이블을 정리하고 dessert 테이블을 만든다.
--
-- 왜 V1을 고치지 않고 V2를 추가하는가:
-- Flyway는 이미 적용된 마이그레이션 파일의 체크섬을 기억하고, 파일 내용이 바뀌면
-- 시작할 때 오류로 막는다. 한번 만든 마이그레이션은 고치지 않고 새 버전으로 덧붙이는 것이
-- 원칙이다. 덕분에 "처음에는 이런 구조였다가 이런 이유로 바꿨다"는 이력이 그대로 남는다.
--
-- 왜 이전 테이블을 지우는가 (요구사항·결정 기록: dessert-trend-docs의 D-03, D-07):
-- 네이버 응답은 저장하지 않고 요청 시점에 가져와 보여주기로 했다(image 테이블 불필요),
-- 랭킹은 보여주지 않는다(keyword_group.rank / relative_ratio 불필요),
-- 후보 키워드 집계는 하지 않는다(candidate_term 불필요).
-- 네 테이블에는 데이터가 없었다(0행). 외래 키로 서로 묶여 있어도 한 문장에서 같이 지우면 된다.
-- ============================================================
DROP TABLE IF EXISTS image, keyword_group, candidate_term, category;

-- 홈 화면에 노출할 디저트 목록. 작성자가 직접 넣는다(요구사항 5.1).
-- keyword  : 화면에 보이면서 네이버에도 보내는 이름. 같은 키워드가 홈에 두 번 뜨지 않게 UNIQUE.
-- visible  : 행을 지우지 않고 홈에서만 뺄 수 있게 하는 스위치.
-- created_at: 서버와 DB의 시간대가 달라도 같은 시각을 가리키도록 시간대 정보가 있는 타입(TIMESTAMPTZ).
CREATE TABLE dessert (
    id         BIGSERIAL PRIMARY KEY,
    keyword    VARCHAR(100) NOT NULL UNIQUE,
    visible    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Supabase는 public 스키마의 테이블을 기본으로 anon 키로도 접근할 수 있게 열어 둔다.
-- 이 서비스는 브라우저가 DB에 직접 붙지 않고 Spring 서버만 DB에 붙으므로,
-- RLS(행 수준 보안)를 켜고 정책을 하나도 만들지 않아서 anon/authenticated 접근을 모두 막는다.
-- (Spring은 DB 소유자 계정으로 접속하므로 영향을 받지 않는다고 알고 있으며, 첫 실행 때 확인한다.)
ALTER TABLE dessert ENABLE ROW LEVEL SECURITY;
