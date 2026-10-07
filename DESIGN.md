# DESIGN.md

화면 작업(프론트)을 할 때마다 반드시 먼저 읽고 따른다. 규칙끼리 충돌하면 **"사용자 요구사항"이 가장 우선**한다.

## 1. 사용자 요구사항 (최우선)
> 사용자가 말한 디자인 요구사항을 여기에 그대로 추가한다. 추가/변경 시 날짜를 적는다.

- (2026-10-07) 아래 "Oceanic Clarity" 레퍼런스(이 파일 하단)를 기준으로 화면을 만든다. 완전히 같을 필요는 없다.
  - 단, **레알 느낌을 위해 골드(`#c9a227`)와 딥 네이비(`#0b1f4b`)를 포인트 색으로 유지**한다. (2026-10-07 사용자 요청) 구조/질감/폰트는 레퍼런스, 포인트 색은 골드.

## 2. 컨셉
- 밝고 투명한 **오션 블루 + 화이트** 베이스에 **골드 + 딥 네이비 포인트**로 레알 느낌을 더한 모바일 웹. 여백이 넉넉하고 은은한 유리(glass) 질감.
- 비공식 팬 사이트: 공식 로고/엠블럼/선수 사진은 쓰지 않는다. 푸터에 "비공식 팬 사이트" 표기 유지.
- 모바일 우선, 반응형. 한국어 UI.

## 3. 디자인 토큰 (`frontend/src/index.css`의 `:root`)
| 토큰 | 값 | 용도 |
|---|---|---|
| `--primary` / `--primary-deep` | `#1e3a8a` / `#00236f` | 제목, 푸터 배경, 히어로 그라데이션 |
| `--secondary` | `#2563eb` | 주요 버튼, 링크, 활성 상태 |
| `--tertiary` | `#38bdf8` | 포커스 링, 강조 숫자, 히어로 포인트 |
| `--accent-soft` | `#e0f2fe` | 배지, 활성 메뉴/하단 바 활성 알약 배경 |
| `--navy` | `#0b1f4b` | **포인트**: 페이지/섹션 제목, 로고, 푸터·히어로 시작색 |
| `--gold` / `--gold-soft` / `--gold-text` | `#c9a227` / `#f6efd2` / `#7a5f00` | **포인트**: 헤더·푸터·히어로 선, 로고, 선수 번호 (배지/활성 표시에는 쓰지 않는다) |
| `--text` / `--muted` / `--muted-2` | `#0f172a` / `#64748b` / `#94a3b8` | 본문 / 보조 / 비활성 |
| `--bg` / `--surface` / `--chip` | `#f8fafc` / `#ffffff` / `#f1f5f9` | 페이지 / 카드 / 칩 |
| `--border` / `--border-input` | `#e2e8f0` / `#cbd5e1` | 카드 / 입력창 테두리 |
| `--radius` / `-lg` / `-xl` | 8px / 16px / 24px | 입력·버튼 / 카드 / 히어로·하단 바 |
| `--shadow-1/2/3` | 하단 레퍼런스 Elevation 참고 | 카드 / 떠 있는 바 / 모달 |
- 색은 토큰으로만 쓴다. 새 색이 필요하면 토큰을 먼저 추가한다. (예외: 카카오 버튼 `#fee500`)

## 4. 타이포그래피
- 폰트: Plus Jakarta Sans (영문/숫자) + **Pretendard (한글 폴백)**. `index.html`에서 로드. (Plus Jakarta에는 한글이 없어서 폴백이 필수)
- 페이지 제목 24/700, 섹션 제목 20/600, 히어로 36(모바일 28)/700, 본문 14~16, 라벨/배지 12/600
- 제목은 자간을 살짝 좁히고(`-0.01~-0.02em`), 본문은 기본 자간.

## 5. 컴포넌트 규칙
- **골드 사용 원칙**: 브랜드 포인트(헤더/푸터/히어로 선, 로고, 선수 번호)에만 쓴다. 배지·칩·활성 표시·버튼은 레퍼런스대로 파랑 계열을 쓴다.
- **헤더**: 반투명 흰색 + blur(16px), sticky, **하단 3px 골드 선**. 로고는 네이비 원형 + 골드 글자. 데스크톱 메뉴는 알약형(활성 = 아이스블루 배경 + 코발트 글자).
- **하단 내비게이션(모바일 <640px)**: 하단에 떠 있는 유리 바(radius 24px), 아이콘 + 라벨 5개, 활성 탭은 아이스블루 알약 표시. 이 폭에서는 상단 메뉴를 숨긴다.
- **버튼**: `btn-primary`(코발트, 높이 48px, 은은한 그림자, hover 시 `--primary`), `btn-soft`, `btn-ghost`, `btn-kakao`. 모서리 8px.
- **카드**: 흰 배경, 1px 테두리, 파란 기운의 약한 그림자, 모서리 16px. 눌렀을 때 scale(0.99).
- **배지**: 높이 28px 알약, 아이스블루 배경 + 코발트 글자.
- **탭/칩**: 높이 36px 알약. 비선택 = 흰색/`--chip` + 짙은 슬레이트 글자, 선택 = 코발트 채움 + 흰 글자.
- **입력창**: 높이 48px, 모서리 8px, 포커스 시 1.5px 코발트 테두리 + 하늘색 링.
- **히어로**: 라운드 24px 그라데이션 배너(navy → primary → secondary), 하단 4px 골드 선, 상단 라벨/`vs`는 골드, 카운트 박스는 골드 테두리의 반투명 유리.
- **선수 카드**: 등번호는 골드 단색.
- **푸터**: `--navy` 배경 + 상단 3px 골드 선, 브랜드명 골드, 서버/DB 상태 표시. 모바일에서는 하단 바에 가리지 않도록 아래 여백 확보.

## 6. 레이아웃
- 컨테이너 최대 1080px, 좌우 여백 모바일 1rem / 태블릿 이상 1.5rem. 섹션 간격은 `space-xl`(2rem).
- 브레이크포인트: 모바일 <640px(1열 스택, 거터 0.75rem), 태블릿 640~1023px(2열 카드, 여백 1.5rem), 데스크톱 ≥1024px(3~4열). 선수 카드는 모바일 2열 / 태블릿 3열 / 데스크톱 4열.
- 구성: `Layout`(Header / Outlet / Footer / BottomNav), 페이지는 `src/pages`, 공통 컴포넌트는 `src/components`.

## 7. 구현 규칙
- 스타일은 `frontend/src/App.css`(레이아웃/컴포넌트)와 `index.css`(토큰/전역)에 둔다. CSS 프레임워크는 아직 쓰지 않는다. (추가하려면 먼저 확인받는다)
- 샘플 데이터는 `src/data/mock.ts`. 실제 API 연동 시 교체한다.
- 접근성: 버튼/링크에 의미 있는 텍스트나 `aria-label`, 포커스 표시(`:focus-visible`) 유지.
- 디자인을 바꿀 때는 이 파일도 함께 갱신한다.
- 아래 "Oceanic Clarity" 레퍼런스는 원본이므로 수정하지 않는다. (사용자가 붙여 넣은 자료)

---
name: Oceanic Clarity
colors:
  surface: '#faf8ff'
  surface-dim: '#d2d9f4'
  surface-bright: '#faf8ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f3ff'
  surface-container: '#eaedff'
  surface-container-high: '#e2e7ff'
  surface-container-highest: '#dae2fd'
  on-surface: '#131b2e'
  on-surface-variant: '#444651'
  inverse-surface: '#283044'
  inverse-on-surface: '#eef0ff'
  outline: '#757682'
  outline-variant: '#c5c5d3'
  surface-tint: '#4059aa'
  primary: '#00236f'
  on-primary: '#ffffff'
  primary-container: '#1e3a8a'
  on-primary-container: '#90a8ff'
  inverse-primary: '#b6c4ff'
  secondary: '#0051d5'
  on-secondary: '#ffffff'
  secondary-container: '#316bf3'
  on-secondary-container: '#fefcff'
  tertiary: '#002e41'
  on-tertiary: '#ffffff'
  tertiary-container: '#00455f'
  on-tertiary-container: '#2eb7f2'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dce1ff'
  primary-fixed-dim: '#b6c4ff'
  on-primary-fixed: '#00164e'
  on-primary-fixed-variant: '#264191'
  secondary-fixed: '#dbe1ff'
  secondary-fixed-dim: '#b4c5ff'
  on-secondary-fixed: '#00174b'
  on-secondary-fixed-variant: '#003ea8'
  tertiary-fixed: '#c4e7ff'
  tertiary-fixed-dim: '#7bd0ff'
  on-tertiary-fixed: '#001e2c'
  on-tertiary-fixed-variant: '#004c69'
  background: '#faf8ff'
  on-background: '#131b2e'
  surface-variant: '#dae2fd'
typography:
  display-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
    letterSpacing: -0.02em
  display-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
    letterSpacing: -0.015em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: 0em
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0em
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0.005em
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
    letterSpacing: 0.01em
  label-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 11px
    fontWeight: '500'
    lineHeight: 14px
    letterSpacing: 0.03em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-mobile: 0.75rem
  margin: 1rem
  margin-mobile: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

This design system embodies an airy, modern, and refreshing aesthetic tailored specifically for high-clarity mobile web experiences. It blends structured minimalism with subtle glassmorphic depth, evoking calm confidence, precision, and technical refinement. 

Targeted at modern mobile users who value fluid navigation and effortless legibility, the interface emphasizes generous whitespace, luminous tonal layering, and optical balance. The visual atmosphere balances crisp oceanic depths with light, icy highlights, delivering an experience that feels light, fast, and immaculately organized.

## Colors

The palette establishes hierarchy through a gradient of oceanic tones set against crisp light surfaces:

- **Primary (`#1E3A8A`):** Deep ocean blue anchors navigational structures, key headers, and authoritative interactive states.
- **Secondary (`#2563EB`):** Vibrant cobalt serves as the primary action driver, highlighting interactive controls, links, and high-priority states.
- **Tertiary (`#38BDF8`):** Sky blue acts as a luminous highlight for active indicators, focus accents, badges, and progressive visual details.
- **Accent Soft (`#E0F2FE`):** Ice blue provides delicate tints for container fills, active chip backgrounds, and frosted glass substrate reflections.
- **Neutrals:** Dark slate (`#0F172A`) drives high-contrast body and title typography, supported by slate tints (`#64748B`, `#94A3B8`) for auxiliary metadata. The canvas rests on pure white (`#FFFFFF`) surfaces over a base canvas of soft slate-white (`#F8FAFC`).

## Typography

The typography uses Plus Jakarta Sans across all display, body, and UI roles. Its contemporary geometric curves and generous x-height maximize legibility on high-density mobile screens while preserving a crisp, premium feel. Tight negative tracking on headlines ensures strong visual grouping, while neutral tracking on body levels enhances rapid scanning on smaller viewports.

## Layout & Spacing

The mobile layout system operates on an 8pt baseline rhythm, optimized for dynamic touch interactions and flexible content flow:

- **Mobile Viewport (< 640px):** 4-column fluid layout with `1rem` margins and `0.75rem` gutters. Elements stack vertically, prioritizing thumb-zone ergonomics.
- **Tablet / Expanded Viewport (640px - 1024px):** 8-column layout with `1.5rem` margins and `1rem` gutters, facilitating two-column card matrices and side-by-side modules.
- **Rhythm & Padding:** Component internal paddings strictly follow the `space-*` scale. Vertical section breaks utilize `space-xl` to maintain an airy, clutter-free rhythm across viewport transitions.

## Elevation & Depth

Visual hierarchy uses a refined hybrid of ambient tinted shadows, translucent tonal tiers, and glassmorphic surfaces:

- **Level 0 (Base Canvas):** Solid `#F8FAFC`, non-elevated.
- **Level 1 (Card & Container Surface):** Solid `#FFFFFF` or `rgba(255, 255, 255, 0.9)` paired with an ultra-soft blue-tinted drop shadow: `0 4px 20px -2px rgba(30, 58, 138, 0.05)`. Edges are framed by a delicate border of `1px solid rgba(224, 242, 254, 0.6)`.
- **Level 2 (Floating Bars & Bottom Navigation):** Translucent frosted glass effect using `background: rgba(255, 255, 255, 0.8)`, `backdrop-filter: blur(16px)`, and an elevated shadow: `0 10px 25px -5px rgba(30, 58, 138, 0.08)`.
- **Level 3 (Modals & Sheets):** Grounded by a deep cobalt ambient veil (`rgba(15, 23, 42, 0.4)`), surfaces use pure `#FFFFFF` with `0 20px 35px -10px rgba(30, 58, 138, 0.15)`.

## Shapes

The interface embraces a balanced rounded corner structure (`roundedness: 2`), delivering smooth visual containment without appearing overly circular:

- **Base Radius (0.5rem / 8px):** Applied to form fields, nested badges, list items, and standard buttons.
- **Large Radius (1rem / 16px):** Standard for content cards, modular containers, bottom sheet top edges, and interactive tiles.
- **Extra-Large Radius (1.5rem / 24px):** Reserved for floating banners, prominent visual heroes, and full-bleed action panels.
- **Pill (Full):** Used exclusively for status chips, micro tags, and floating tab pills to maintain instant interactive distinction.

## Components

- **Buttons:**
  - *Primary:* Solid vibrant cobalt (`#2563EB`) background, white text, subtle shadow (`0 4px 12px rgba(37, 99, 235, 0.25)`). Active state darkens to `#1E3A8A`. Minimum height 48px for touch target accessibility.
  - *Secondary:* Soft ice blue background (`#E0F2FE`), cobalt text (`#2563EB`), zero border.
  - *Ghost / Outline:* Transparent fill, `1px` border of `rgba(37, 99, 235, 0.3)`, cobalt text.

- **Chips & Badges:**
  - Compact pill-shaped elements (`rounded-full`, 28px height).
  - Unselected: Crisp white or `#F1F5F9` background with dark slate text (`#0F172A`).
  - Selected: Vibrant cobalt background with white text, or sky blue accent tint (`#E0F2FE`) with cobalt typography.

- **Cards:**
  - Rendered with `rounded-lg` (16px) corners, clean white fill, and a subtle border (`1px solid #E2E8F0`).
  - Interactive cards feature an active touch downstate that subtly scales (0.99) and increases shadow tint to deep ocean blue.

- **Input Fields:**
  - 48px height with `rounded` (8px) corners, white background, and neutral slate border (`#CBD5E1`).
  - Focus state highlights with a crisp 1.5px cobalt border (`#2563EB`) and an exterior sky blue ring (`0 0 0 3px rgba(56, 189, 248, 0.25)`).

- **Checkboxes & Radios:**
  - 20px size with smooth corners (4px for checkbox, full circle for radio).
  - Inactive: `#FFFFFF` fill with `1.5px` border in `#94A3B8`.
  - Active: Vibrant cobalt fill (`#2563EB`) with crisp white check or center pip.

- **Bottom Navigation Bar:**
  - Fixed mobile bottom dock using glassmorphism (`rgba(255, 255, 255, 0.85)` with `backdrop-filter: blur(12px)`).
  - Uses an active pill indicator in sky blue tint (`#E0F2FE`) under current tab icons.