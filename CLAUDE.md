# CLAUDE.md

이 파일은 Claude가 매 세션 시작 시 읽고 따르는 프로젝트 지침입니다.

## 프로젝트 맥락
- 서비스: **Hala Madrid** — 레알 마드리드 **비공식** 팬 커뮤니티/정보 서비스 (포트폴리오 + 실사용)
- 기획서: `docs/PLAN.md` (기능, 화면, DB, API, 개발 단계. 충돌하면 이 파일이 기준)
- 스택
  - Frontend: React + Vite + TypeScript → Vercel (`frontend/`)
  - Backend: Spring Boot 4 (Java 21, Gradle) → Render Web Service, Docker (`backend/`)
  - DB: MySQL (외부 무료 호스팅: TiDB Cloud 또는 Aiven. Render는 MySQL 미제공)
  - VCS: GitHub 모노레포 `https://github.com/09170917jun-design/Hala-Mardrid.git` (브랜치 `main`)
- 디자인: **화면 작업 전에 항상 `DESIGN.md`를 읽고 따른다.** (오션 블루 + 화이트 "Oceanic Clarity" 베이스에 골드/딥 네이비 포인트, 사용자 요구사항 포함. 디자인을 바꾸면 DESIGN.md도 갱신)
- 공식 로고/엠블럼/선수 사진은 사용하지 않는다. 푸터에 "비공식 팬 사이트" 표기.

## 작업 규칙
1. **푸시/배포는 사용자가 승인했을 때만** 한다. 커밋은 로컬에서 자유롭게 하되 `git push`는 먼저 물어본다.
2. **비밀 값 금지**: `.env`, 키, 비밀번호, Client Secret은 절대 커밋하지 않는다. 채팅에 붙여넣으라고 요청하지 않는다. 템플릿은 `backend/.env.example`.
3. 한국어로 답한다. 설명은 짧게, 결과(무엇이 바뀌었고 어떻게 확인하는지) 위주로.
4. 코드는 기존 스타일에 맞춘다. 요청하지 않은 기능/리팩터링을 추가하지 않는다.
5. 변경 후에는 가능한 검증을 한다: 프론트 `npm run build` + `npm run lint`, 백엔드 `gradlew test`.
6. 큰 결정(라이브러리 추가, DB 구조 변경 등)은 추천안 하나를 제시하고 확인받는다.
7. **수정이 필요하거나 이상한 점을 발견하면 바로 알린다.** 지침/기획서와 실제 코드가 어긋날 때, 보안·배포·비용 위험, 외부 서비스 정책 변경 가능성, 지침 자체의 오류가 해당한다. 조용히 넘어가거나 임의로 우회하지 않는다.

## 환경 주의사항 (Windows / PowerShell 5.1)
- PowerShell의 `Set-Content -Encoding utf8`은 **BOM을 붙여** Java 컴파일/설정 파일을 깨뜨린다. 파일 작성은 Write/Edit 도구를 쓴다. (긴 Bash heredoc 여러 개를 한 번에 실행하면 파싱 오류가 날 수 있다.)
- JDK 21 경로: `C:\Program Files\Eclipse Adoptium\jdk-21*`. 새 터미널이 아니면 `JAVA_HOME`을 직접 지정한다.
- Docker는 설치하지 않았다. **개발 DB는 로컬 MySQL 8.3(서비스 `MySQL83`, 3306)** 을 쓴다. DB `hala_madrid`, 전용 사용자 `hala`. 접속 정보는 `backend/.env`(`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`)에 있고, DB/사용자 생성 SQL은 `backend/db/local-init.local.sql`(git 제외)에 있다. 배포(Render)에서만 외부 MySQL을 쓰며 같은 이름의 환경변수로 주입한다.
- **배포용 DB(Aiven 무료 MySQL 8.4)**: 서비스 `mysql-39563ad3`, 호스트 `mysql-39563ad3-jun-2980.c.aivencloud.com`, 포트 `16509`, DB `defaultdb`, 사용자 `avnadmin`, SSL 필수. 비밀번호는 Aiven 콘솔에서만 확인하고 Render 환경변수에 직접 입력한다(채팅/파일 금지). 무료 플랜은 **비활성 시 자동 전원 꺼짐**, 디스크 1GB. 접속 URL: `jdbc:mysql://<호스트>:16509/defaultdb?sslMode=REQUIRED&serverTimezone=Asia/Seoul&characterEncoding=utf8`
- Aiven에는 실수로 만든 유료(Developer-1) PostgreSQL `pg-1a815a72`가 있다. 사용자가 직접 삭제하기로 했다. 체험 크레딧($50, 30일) 종료 전에 삭제 여부를 확인한다.
- 백엔드는 DB에 접속할 수 없으면 기동되지 않는다(JPA). 로컬 MySQL이 꺼져 있거나 `.env`가 없으면 먼저 확인한다.
- 테스트는 `src/test/resources/application.properties` 설정으로 DB 없이 컨텍스트가 뜬다.

## 실행 방법
```
# 백엔드 (http://localhost:8080)
cd backend
.\gradlew.bat bootRun

# 프론트 (http://localhost:5173, /api는 8080으로 프록시)
cd frontend
npm run dev
```

## 현재 진행 상황 (작업할 때마다 갱신)
- [x] 0단계: Git 초기화, 기획서, 뼈대(백엔드 헬스체크, 프론트 Vite)
- [x] 레이아웃/디자인 목업: 헤더, 푸터, 홈, 경기, 선수, 게시판, 로그인 (샘플 데이터 `frontend/src/data/mock.ts`) — 사용자 확인 대기
- [x] 1단계: Hello World 배포 완료 (2026-10-07). 프론트 `https://hala-mardrid.vercel.app` (Vercel Hobby, root `frontend`), 백엔드 `https://hala-madrid-backend.onrender.com` (Render Free, Docker, root `backend`, Singapore), DB Aiven MySQL. `/api/health`가 Vercel 프록시를 거쳐 `db: up` 확인. main 브랜치에 푸시하면 Render/Vercel이 자동 재배포한다. Render 환경변수: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- [ ] 2단계: 인증 — **이메일 가입/로그인/로그아웃/토큰 갱신 구현, 로컬·배포 환경 모두 검증 완료(2026-10-07, `7b098e0`)**. Render `JWT_SECRET` 설정 완료. 배포 DB에 확인용 계정 `deploy.check@example.com`(닉네임 `deploy_check`)이 있음(백엔드 테스트 9개, 브라우저 확인). 구조: 액세스 JWT(15분, 메모리) + 리프레시 토큰(14일, httpOnly 쿠키 `refresh_token`, path `/api/auth`, DB엔 SHA-256 해시, 1회용 회전). **남은 일**: 카카오 로그인(Client Secret은 사용자가 `backend/.env`에 입력, Redirect URI `http://localhost:5173/auth/kakao/callback`, `https://hala-mardrid.vercel.app/auth/kakao/callback` 등록 필요, 프론트 카카오 버튼은 현재 비활성)
- [ ] 3단계: 게시판/댓글/좋아요
- [ ] 4단계: 선수단 + 경기 동기화 (football-data.org)
- [ ] 5단계: 선수 평점 투표
- [ ] 6단계: 마무리(디자인, 테스트, README)
