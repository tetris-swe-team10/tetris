# tetris

Java21 / JavaFX21 / Gradle 기반 소프트웨어공학 팀 프로젝트.

Windows 11 x64 배포 ZIP 생성: `./gradlew.bat :app:packageWindowsZip`

결과: `app/build/distributions/Tetris-Windows-x64.zip`. 전체를 압축 해제한 후 `Tetris/Tetris.exe`를 실행합니다.
아이콘과 전용 Java 21 런타임이 포함되므로 받는 사람은 Java를 설치하지 않아도 됩니다.
패키지 자동 검증: `./gradlew.bat :app:verifyWindowsPackage`. [배포 방법](docs/distribution.md)

프로젝트 관리·AI 작업 기준은 [문서 지도](docs/README.md)에서 시작합니다.
사람과 AI의 읽는 순서·역할별 요청·리뷰 후 병합 기준은 [read.md](read.md)에 있습니다.
[AI 지침](docs/ai-guide.md), [품질·평가 근거](docs/quality-and-evaluation.md), [노션 정리·원문](docs/notion/README.md)을 확인할 수 있습니다.

## 프로젝트 관리

진행 관리는 GitHub 이슈·마일스톤·PR로 하고, 계획·회의 기록은 노션에서 작성해 [저장소에 보관](docs/notion/README.md)합니다.

- 개발 주기별 할 일: [마일스톤](https://github.com/tetris-swe-team10/tetris/milestones?state=all) — 1차 기본 게임, 2차 아이템 모드, 3차 대전 모드
- 팀원별 업무: 역할 라벨 [A·혜원](https://github.com/tetris-swe-team10/tetris/issues?q=label%3A%22%EC%97%AD%ED%95%A0+A%C2%B7%ED%98%9C%EC%9B%90%22), [B·수연](https://github.com/tetris-swe-team10/tetris/issues?q=label%3A%22%EC%97%AD%ED%95%A0+B%C2%B7%EC%88%98%EC%97%B0%22), [C·서현](https://github.com/tetris-swe-team10/tetris/issues?q=label%3A%22%EC%97%AD%ED%95%A0+C%C2%B7%EC%84%9C%ED%98%84%22)과 이슈 담당자
- 역할 배분과 이유: [역할별 시작점](docs/tasks/README.md), 노션 「Agile Planning」·「3차 요구사항 역할 분담 · 2주 계획」([노션 정리](docs/notion/README.md))
- 코드 리뷰: [병합된 PR](https://github.com/tetris-swe-team10/tetris/pulls?q=is%3Apr+is%3Amerged)의 리뷰 기록
- 최종 평가 점검: [시연 체크리스트](docs/tasks/demo-checklist.md)

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
