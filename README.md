# tetris

Java21 / JavaFX21 / Gradle 기반 소프트웨어공학 팀 프로젝트.

Windows 11 x64 배포 ZIP 생성: `./gradlew.bat :app:packageWindowsZip`

결과: `app/build/distributions/Tetris-Windows-x64.zip`. 전체를 압축 해제한 후 `Tetris/Tetris.exe`를 실행합니다.
아이콘과 전용 Java 21 런타임이 포함되므로 받는 사람은 Java를 설치하지 않아도 됩니다.
패키지 자동 검증: `./gradlew.bat :app:verifyWindowsPackage`. [배포 방법](docs/distribution.md)

프로젝트 관리·AI 작업 기준은 [문서 지도](docs/README.md)에서 시작합니다.
사람과 AI의 읽는 순서·역할별 요청·리뷰 후 병합 기준은 [read.md](read.md)에 있습니다.
[AI 지침](docs/ai-guide.md), [품질·평가 근거](docs/quality-and-evaluation.md), [노션 정리·원문](docs/notion/README.md)을 확인할 수 있습니다.

```powershell
.\gradlew.bat :app:run
.\gradlew.bat :app:test
.\gradlew.bat :app:uiTest
```

`uiTest`는 실제 JavaFX 창을 사용하는 데스크톱 테스트이며 일반 로직 테스트와 분리되어 있습니다.

게임 시작 전에 난이도를 선택합니다. 한 판 동안 난이도를 변경할 수 없고, 기록은 모드·난이도별로 구분합니다.

점수·난이도 규칙, MVC 책임 분리, 삭제 상태와 팀원 연동 계약은 [구현 설명](docs/scoring-difficulty.md)을 참고하세요.

현재 블록의 착지 위치를 고스트 테두리로 표시하고, 좌우 끝에서 세로 점선으로 낙하 범위를 안내합니다.
이동·회전 시 갱신하며 B 암전 중에는 숨깁니다. [고스트 블록 명세](docs/ghost-block.md)

아이템 모드에서 10줄마다 새 NEXT에 작은 폭탄 `E`가 붙습니다.
고정 시 해당 칸 중심 3×3 안의 삭제 대상이 0.3초간 깜빡인 뒤 사라집니다.
폭발 자체로 점수·줄 수를 올리거나 위 블록을 떨어뜨리지 않습니다.
경계 조건과 추가 아이템 연동은 [작은 폭탄 명세](docs/small-bomb.md)를 참고하세요.
줄 삭제 `L`은 고정 시 L이 있는 줄을 꽉 차지 않아도 삭제하며, 점수·줄 수는 일반 줄 삭제와 같습니다. [줄 삭제 명세](docs/line-clear-item.md)
무게추 `W`는 너비 4칸 블록으로, 처음 닿으면 좌우가 잠기고 아래 4열을 한 칸씩 뚫으며 지운 뒤 바닥에서 사라집니다(파괴분 무득점). [무게추 명세](docs/weight-item.md)

전체 코드의 Line Coverage 보고서는 아래 명령으로 생성합니다. `uiTest`는 데스크톱이 필요합니다.

```powershell
.\gradlew.bat :app:test :app:uiTest :app:jacocoTestReport
```

보고서: `app/build/reports/jacoco/test/html/index.html`

## 팀원이 읽을 문서 (2026-11-18)

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
├─ README.md              이 파일: 실행·배포, 읽을 문서, 구조
├─ read.md                AI·사람의 작업 읽는 순서
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
