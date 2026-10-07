# Hala Madrid (비공식 팬 서비스) 기획서

## 1. 개요
- 레알 마드리드 팬을 위한 **비공식** 커뮤니티/정보 서비스. 포트폴리오 + 실사용 가능 수준.
- 푸터에 "비공식 팬 사이트" 명시. 공식 로고/엠블럼/선수 사진 미사용.

## 2. 기술 스택 / 배포
| 영역 | 기술 | 배포 |
|---|---|---|
| Frontend | React (Vite, TypeScript), React Router, TanStack Query | Vercel |
| Backend | Spring Boot 3 (Java 21), Spring Security, JPA | Render Web Service (Docker) |
| DB | MySQL | 외부 무료 MySQL (TiDB Cloud Serverless 또는 Aiven) |
| VCS | Git / GitHub 모노레포 (`frontend/`, `backend/`, `docs/`) | - |

- 프로필 분리: `local`(Docker MySQL) / `prod`(환경변수로 외부 MySQL 접속).
- Vercel `rewrites`로 `/api/*`를 Render 백엔드로 프록시 → 동일 출처가 되어 CORS/쿠키 문제 완화.

## 3. MVP 기능 (1차)
1. **회원/인증**: 이메일 가입·로그인, 카카오 로그인. JWT(Access + Refresh, Refresh는 httpOnly 쿠키).
2. **자유게시판**: 글 CRUD, 댓글, 좋아요, 페이징, 본인 글만 수정/삭제, 신고(관리자 삭제).
3. **경기 일정/결과**: 외부 API 데이터를 DB에 동기화해 표시(예정/종료 구분).
4. **선수단**: 선수 목록(포지션 필터), 선수 상세.
5. **경기 후 선수 평점 투표**: 종료된 경기의 출전 선수에 1~10점(0.5 단위) 투표, 평균 평점 표시. 경기·선수·유저당 1회.

### 2차 (여유 시)
- 승부 예측 게임/랭킹, 다크 모드, 알림.

## 4. 화면 구성
| 경로 | 화면 |
|---|---|
| `/` | 홈: 다음 경기 카운트다운, 최근 결과, 인기 글 |
| `/matches` | 경기 일정/결과 목록 |
| `/matches/:id` | 경기 상세 + 선수 평점 투표/평균 |
| `/players`, `/players/:id` | 선수단 / 선수 상세 |
| `/board`, `/board/:id`, `/board/write` | 게시판 목록 / 상세 / 작성 |
| `/login`, `/signup` | 로그인(이메일·카카오) / 가입 |
| `/me` | 내 정보, 내가 쓴 글 |

## 5. 디자인 방향
- 컨셉: 클래식하고 깔끔한 "화이트 + 네이비 + 골드" (레알의 흰 유니폼과 격조 있는 분위기).
- 색상 토큰: 배경 `#FFFFFF`/`#F6F7FB`, 메인 네이비 `#0B1F4B`, 포인트 골드 `#C9A227`, 텍스트 `#1A1A1A`.
- 폰트: Pretendard(본문), 제목은 세리프 계열 포인트. 반응형(모바일 우선).

## 6. DB 설계 (MySQL)
```
users(id, email UNIQUE, password_hash NULL, nickname UNIQUE, role[USER/ADMIN], created_at)
social_accounts(id, user_id FK, provider[KAKAO], provider_id, UNIQUE(provider, provider_id))
refresh_tokens(id, user_id FK, token_hash, expires_at)

posts(id, user_id FK, title, content, view_count, created_at, updated_at, deleted)
comments(id, post_id FK, user_id FK, content, created_at, deleted)
post_likes(id, post_id FK, user_id FK, UNIQUE(post_id, user_id))
reports(id, post_id FK, user_id FK, reason, created_at)

players(id, external_id, name, position, shirt_number, nationality, birth_date, active)
matches(id, external_id, competition, home_team, away_team, kickoff_at,
        status[SCHEDULED/FINISHED], home_score, away_score)
match_players(id, match_id FK, player_id FK)          -- 출전 선수
player_ratings(id, match_id FK, player_id FK, user_id FK, score DECIMAL(3,1),
               UNIQUE(match_id, player_id, user_id))
```
- 평균 평점은 조회 시 집계(초기), 트래픽이 늘면 캐시 컬럼 추가.

## 7. API 설계 (REST, prefix `/api`)
| 영역 | 엔드포인트 |
|---|---|
| Auth | `POST /auth/signup`, `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout`, `POST /auth/kakao` (인가 코드 전달) |
| User | `GET /users/me`, `PATCH /users/me` |
| Posts | `GET /posts?page&size`, `GET /posts/{id}`, `POST /posts`, `PUT /posts/{id}`, `DELETE /posts/{id}`, `POST /posts/{id}/like`, `POST /posts/{id}/report` |
| Comments | `GET /posts/{id}/comments`, `POST /posts/{id}/comments`, `DELETE /comments/{id}` |
| Matches | `GET /matches?status`, `GET /matches/{id}` |
| Players | `GET /players?position`, `GET /players/{id}` |
| Ratings | `GET /matches/{id}/ratings`, `PUT /matches/{id}/ratings/{playerId}` (본인 1회, 수정 가능) |

- 공통 에러 응답 포맷, 입력 검증(Bean Validation), 쓰기 API는 인증 필수.
- 경기/선수 동기화: 스케줄러(`@Scheduled`)로 football-data.org를 주기 호출해 DB 저장.

## 8. 비기능/보안
- 비밀번호 BCrypt, 시크릿은 환경변수(Render/Vercel), `.env` 커밋 금지.
- Render 무료 플랜 콜드스타트(30~60초) 감수, 프론트에 로딩 안내 표시.
- 게시글 XSS 방지(출력 이스케이프), 로그인 시도 제한은 2차.

## 9. 개발 단계
| 단계 | 내용 |
|---|---|
| 0 | 모노레포 생성, GitHub 연결, 로컬 Docker MySQL |
| 1 | **Hello World 배포**: Vercel + Render + 외부 MySQL 연결 확인 |
| 2 | 인증(이메일 → 카카오) |
| 3 | 게시판/댓글/좋아요 |
| 4 | 선수단 + 경기 동기화 |
| 5 | 평점 투표 |
| 6 | 디자인 다듬기, 테스트, 문서(README) |

## 10. 사전 준비 체크리스트
- [ ] 카카오 개발자 계정 + 앱 등록(REST API 키, Redirect URI)
- [ ] football-data.org API 키 발급
- [ ] 외부 MySQL 가입(TiDB Cloud 또는 Aiven)
- [ ] GitHub 저장소 생성
