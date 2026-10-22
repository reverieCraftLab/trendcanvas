# trendcanvas (백엔드) 작업 규칙

디저트 트렌드 갤러리 사이트의 백엔드다. Spring Boot 4.1.0, Java 21, JPA, Flyway, PostgreSQL(Supabase).

## 시작할 때 먼저 읽을 것

요구사항과 결정 기록은 다른 저장소에 있다. 작업을 시작하기 전에 아래를 읽고 현재 상태와 결정을 파악한다.

- `../dessert-trend-docs/docs/next-steps.md`: 현재 상태와 다음 할 일
- `../dessert-trend-docs/docs/decisions.md`: 결정과 이유
- `../dessert-trend-docs/docs/01-requirements.md`: 요구사항

위 경로에 없으면 https://github.com/reverieCraftLab/dessert-trend-docs 의 같은 파일을 본다.

## 작업 규칙

- 답변은 한글로 한다. 코드를 만들면 왜 그렇게 했는지 설명한다. 특히 DB 설계와 트랜잭션 결정은 근거를 자세히 설명한다.
- 사용자(GT)가 정하지 않은 요구사항은 채우지 않는다. 초안과 이유를 보여주고 합의한 뒤 진행한다.
- 코드는 Claude가 만들고, GT가 IntelliJ에서 검토·실행하고 커밋·푸시한다. 결정과 이유는 문서 저장소의 `decisions.md`에 기록한다.
- 새 클래스를 추가하면 컴파일 통과만으로 끝내지 않고 앱을 띄워 확인한다(빈 주입 오류는 시작할 때 드러난다).
- 외부 API를 연동하면 실제 응답 한 번을 확인한다(네이버가 JSON을 `text/plain`으로 보낸 전례가 있다).

## 구조

기능별 폴더 아래에 계층 폴더를 둔다(D-16). 파일 이름에 종류를 붙인다(`...Controller`, `...Service`).

```
dessert/{controller,service,repository,domain,dto}
image/{controller,service}
naver/{client,config,dto,exception}
common/exception
```

## 지켜야 할 것

- **비밀 값을 코드, 설정 파일, 문서, 채팅에 적지 않는다.** DB 접속 정보와 네이버 키는 환경 변수(`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`)로만 받는다. `application-local.yml`, `application-prd.yml`은 `.gitignore`에 있다. IntelliJ가 만드는 `.mcp.json`은 커밋하지 않는다.
- **네이버 결과를 저장·캐시·가공하지 않는다**(NFR-05, 약관 7.3③). DB에는 우리가 만든 데이터(디저트 목록, 행동 기록)만 둔다. 응답은 가공 없이 전달한다.
- **테이블은 Flyway 마이그레이션으로만 만든다**(`ddl-auto: validate`). 이미 적용된 마이그레이션 파일은 고치지 않고 새 버전을 추가한다.
- 네이버는 잘못된 요청과 0건도 호출 1회로 세므로(NFR-04) 입력은 네이버로 보내기 전에 검증한다. 429는 재시도하지 않는다.
- 커밋 메시지는 `타입: 한 줄 요약` 형태로 쓴다(feat, refactor, fix, docs).
- 개발 중에는 `local` 프로파일(로컬 도커 DB)로 실행한다. `prd`는 Supabase에 붙으므로 필요한 순간에만 쓴다.
