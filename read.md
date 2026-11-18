# 사람과 AI의 작업 안내

> 갱신: 2026-11-18. 이 파일은 AI가 매 세션 읽는 프로젝트 문맥이며 모델을 영구 학습시키는 파일은 아니다.

1. [AGENTS.md](AGENTS.md) → [시작 루틴](docs/session-routines.md)을 읽고 브랜치·미커밋·원격·인수인계를 확인한다.
2. [역할별 시작점](docs/tasks/README.md)에서 A 혜원 / B 수연 / C 서현 중 자신의 문서만 먼저 읽는다.
3. [협업 규칙](docs/coordination/README.md)과 나에게 온 미완료 요청·질문·제안 의견을 확인한다.
4. 필요한 코드·명세만 읽고 구현한다. 변경 부분과 직접 영향받는 테스트만 실행한다.
5. 이슈 → 기능 브랜치 → 커밋·push → PR → 다른 사람의 리뷰 코멘트 → 수정·답글 → 병합 순서로 진행한다.

## 우리가 지금 읽을 것 (2026-11-18)

### 세 사람 공통

| 문서 | 왜 읽나 |
| --- | --- |
| [시연 체크리스트](docs/tasks/demo-checklist.md) | 평가 항목 1-1~8-31별 구현 근거·부족한 부분. 시연 칸은 실제 실행한 결과만 채운다 |
| [역할별 시작점](docs/tasks/README.md) | A/B/C 책임, 공통 완료 기준, 역할 배분 이유 |
| [3차 역할·인터페이스](docs/coordination/req3-roles.md) | 대전·P2P에서 서로 넘겨주는 클래스와 값 |
| [작업 현황](docs/coordination/status.md) | 최근 인수인계와 남은 일 |
| [품질·평가 근거](docs/quality-and-evaluation.md) | 테스트 선별 실행, 커버리지 측정 방법 |
| [진행 보드](https://github.com/orgs/tetris-swe-team10/projects/1) · [마일스톤](https://github.com/tetris-swe-team10/tetris/milestones?state=all) | 이슈 상태와 개발 주기별 할 일. 새 이슈는 마일스톤과 역할 라벨을 붙인다 |

### 역할별

| 역할 | 먼저 읽을 문서 | 나에게 온 요청·의견 요청 |
| --- | --- | --- |
| A 혜원 | [A 할 일](docs/tasks/A-local-versus.md), [A 평가 시연](docs/tasks/A-evaluation.md)(PR #66 병합 후) | 없음 |
| B 수연 | [B 할 일](docs/tasks/B-network-lobby.md) | [R-A-003 체크리스트 확인](https://github.com/tetris-swe-team10/tetris/issues/67), [P-A-002 리디자인 의견](docs/coordination/proposals/P-A-002.md) |
| C 서현 | [C 할 일](docs/tasks/C-design-p2p-game.md) | [R-A-003 체크리스트 확인](https://github.com/tetris-swe-team10/tetris/issues/67), [P-A-002 리디자인 의견](docs/coordination/proposals/P-A-002.md), PR #66·#68 리뷰 |

### 지켜야 할 것

- 동료 리뷰 후 병합. 작성자·AI의 자체 점검은 리뷰로 인정하지 않는다. 자동 병합은 쓰지 않는다.
- 제안·요청·질문 문서는 PR 없이 main에 바로 올린다(D-011). 코드·명세·운영 문서는 PR로 올린다.
- `app/bin` 같은 IDE 산출물과 개인 설정은 커밋하지 않는다. 선별 테스트 통과를 커버리지 70% 달성으로 표현하지 않는다.

## 디렉토리 구조

```text
tetris/
├─ README.md              실행·배포 방법, 프로젝트 관리 링크
├─ read.md                이 파일: 읽는 순서와 지금 읽을 것
├─ AGENTS.md, CLAUDE.md   AI 공통 작업 규칙
├─ app/
│  ├─ build.gradle        빌드·테스트(test/uiTest)·JaCoCo·Windows 패키징
│  ├─ bin/                IDE 산출물(커밋 금지)
│  └─ src/
│     ├─ main/java/com/team10/tetris/
│     │  ├─ App, Launcher         프로그램 시작점
│     │  ├─ controller/           화면 전환(AppController)
│     │  ├─ game/                 게임 규칙: 보드·블록·점수·난이도·엔진
│     │  │  ├─ item/              아이템 E·L·W·B·K
│     │  │  └─ versus/            같은 PC 대전: 공격 줄·승패·시간제한
│     │  ├─ input/                키 설정·입력(1인·2인·메뉴)
│     │  ├─ net/                  P2P 연결 계약(인터페이스·메시지)
│     │  ├─ score/                스코어보드 저장·순위
│     │  ├─ screen/               시작 메뉴·모드 선택 화면
│     │  ├─ settings/             설정 화면·저장·키 설정 창
│     │  └─ ui/                   게임 화면·보드 그리기·종료 화면·스코어보드
│     ├─ main/resources/com/team10/tetris/   CSS·폰트·아이콘
│     └─ test/java/com/team10/tetris/        main과 같은 패키지별 테스트
├─ docs/
│  ├─ README.md           문서 지도
│  ├─ tasks/              역할별 할 일, 시연 체크리스트, A 평가 시연
│  ├─ coordination/       협업 기록
│  │  ├─ proposals/       제안서 P-역할-번호
│  │  ├─ requests/        요청서 R-역할-번호
│  │  ├─ questions.md     질문 Q-번호
│  │  ├─ decisions.md     결정 D-번호
│  │  ├─ status.md        현황·인수인계
│  │  └─ req3-roles.md    3차 역할·인터페이스
│  ├─ notion/             노션 정리와 원문 보관(archive는 수정 금지)
│  └─ *.md                기능 명세: 점수·설정·고스트·아이템·폰트·배포
├─ packaging/             Windows 배포 안내문
├─ tools/                 아이콘 생성, Windows 패키지 검증 스크립트
├─ gradle/, gradlew*      Gradle wrapper·의존성 버전
└─ .github/               이슈·PR 템플릿
```

## 문맥의 우선순위

현재 사용자 지시 → 실제 팀 승인과 교수님 원문 → 현재 코드·검증 근거 → 과거 문서·아이디어 순으로 판단한다.
제안·요청·질문은 지시나 승인으로 자동 승격되지 않는다. 다른 사람의 의견이나 완료 여부를 대신 작성하지 않는다.
새 합의는 [결정 기록](docs/coordination/decisions.md), 진행은 [현황](docs/coordination/status.md)에 남긴다.
