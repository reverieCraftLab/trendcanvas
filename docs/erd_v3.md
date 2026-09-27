# ERD v3 — TrendCanvas (Dessert MVP)

## 개요

디저트 트렌드 발견 파이프라인의 데이터를 저장하는 5개 테이블 구조.
카테고리는 정규화된 별도 테이블로 분리해, 이후 디저트 외 카테고리(패션, 여행 등)로
확장할 때 스키마 변경 없이 데이터만 추가되도록 설계했다.

## 전체 파이프라인 흐름

```
1. 시드 키워드로 Naver 블로그/뉴스 API 검색
2. HTML 태그 제거
3. 형태소 분석으로 조사/어미 마스킹 (KoNLPy는 단어 추출기가 아니라 필터로 사용)
4. n-gram(2~4자 슬라이딩 윈도우) 추출
5. 빈도 + 소스 다양성 카운팅
6. 7일 이동 평균 대비 스파이크 탐지
7. 상위 5개 후보를 KEYWORD_GROUP으로 승격
8. Naver Search Trend API로 5개 그룹 간 상대 비교
9. 이미지 소스에서 관련 이미지 검색 및 저장
```

## 테이블 정의

### 1. CATEGORY

카테고리 마스터 테이블. 현재는 디저트만 존재하지만, 확장을 고려해 처음부터 분리.

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT (PK) | |
| name | VARCHAR | 카테고리명 (예: "디저트") |
| created_at | TIMESTAMP | |

### 2. CANDIDATE_TERM

n-gram 추출 및 스파이크 탐지 과정에서 나온 모든 후보 키워드를 저장하는 원천 테이블.

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT (PK) | |
| category_id | BIGINT (FK → CATEGORY) | |
| term | VARCHAR | n-gram으로 추출된 후보 문자열 |
| raw_payload | JSONB | 원본 응답 데이터 (블로그/뉴스 API raw response) |
| source | VARCHAR | 데이터 출처 (blog / news 등) |
| created_at | TIMESTAMP | |

### 3. TERM_FREQUENCY_LOG

후보 키워드별 일자별 빈도/소스 다양성 기록. 7일 이동 평균 계산과 스파이크 탐지의 근거 데이터.

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT (PK) | |
| candidate_term_id | BIGINT (FK → CANDIDATE_TERM) | |
| log_date | DATE | 집계 기준일 |
| frequency_count | INT | 해당일 등장 빈도 |
| source_diversity_count | INT | 해당일 서로 다른 출처 수 |
| created_at | TIMESTAMP | |

### 4. KEYWORD_GROUP

스파이크 탐지를 통과해 상위 5개로 선정된 후보군. Naver Search Trend API 호출 단위(최대 5개 그룹)와 1:1 매칭.

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT (PK) | |
| category_id | BIGINT (FK → CATEGORY) | |
| candidate_term_id | BIGINT (FK → CANDIDATE_TERM) | |
| relative_ratio | DECIMAL | Search Trend API가 반환한 상대 비교 수치 |
| promoted_at | TIMESTAMP | 후보에서 승격된 시각 |
| status | VARCHAR | 처리 상태 (pending / validated / image_fetched 등) |

### 5. IMAGE

키워드 그룹별로 수집된 이미지. 이미지 소스(Unsplash, Daum, Google 등)가 아직 미정이므로
provider-agnostic하게 필드를 설계.

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT (PK) | |
| keyword_group_id | BIGINT (FK → KEYWORD_GROUP) | |
| source | VARCHAR | 이미지 제공처 (unsplash / daum / google 등, 특정 API에 종속되지 않도록 문자열로 관리) |
| source_id | VARCHAR | 제공처 측 이미지 고유 ID |
| source_page_url | VARCHAR | 원본 페이지 URL |
| image_url | VARCHAR | 실제 이미지 파일 URL |
| created_at | TIMESTAMP | |

## 관계 요약

```
CATEGORY 1 ── N CANDIDATE_TERM
CANDIDATE_TERM 1 ── N TERM_FREQUENCY_LOG
CANDIDATE_TERM 1 ── N KEYWORD_GROUP
CATEGORY 1 ── N KEYWORD_GROUP
KEYWORD_GROUP 1 ── N IMAGE
```

## 설계 시 짚어야 할 트랜잭션 포인트

- **UPSERT idempotency**: 동일 후보 키워드가 여러 날짜에 걸쳐 재수집될 수 있으므로,
  CANDIDATE_TERM / TERM_FREQUENCY_LOG 저장 시 `ON CONFLICT`(DB 레벨) 처리가 필요.
  애플리케이션에서 select-then-write로 처리하면 동시 요청 시 race condition 발생 가능.
- **상태 전이 트랜잭션**: 후보 5개를 KEYWORD_GROUP으로 승격하는 작업은 여러 행을 동시에
  쓰는 작업이므로 `@Transactional`로 묶어야 함. 5개를 하나의 트랜잭션으로 묶을지,
  개별 트랜잭션으로 처리할지는 실패 시 재시도 전략에 따라 결정 필요.

---
*이 문서는 v3 기준이며, DDL 작성 과정에서 세부 타입/제약조건은 조정될 수 있습니다.*