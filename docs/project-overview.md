# 프로젝트 개요와 코드 지도

> 확인 기준: main `c68d0fb` · 패키징은 PR #54로 별도 확인 · 정리일: 2026-11-09 · 이번 정리는 정적 확인이며 기능 재실행 결과가 아님

## 목적·환경

JavaFX 테트리스에 일반/아이템 모드, 난이도, 기록, 설정, 색맹 표시와 고스트를 제공한다.
현재 기준은 Java 21, JavaFX 21, Gradle Wrapper 9.2.0, JUnit Jupiter, JaCoCo다. GUI 테스트와 Windows 패키지 검증은 데스크톱 환경이 필요하다.

```powershell
.\gradlew.bat :app:run
.\gradlew.bat :app:assemble
```

환경 오류를 피하려고 개인 PC에서 사용한 Gradle 캐시 경로를 모두에게 강제하지 않는다. 문제가 재현될 때 해당 PC의 JDK·경로·캐시·로그를 확인한다. Configuration Cache 관련 오류가 실제 발생하면 `--no-configuration-cache`로 분리 확인한다.

## 책임 분리

| 기능 | 코드 |
| --- | --- |
| 실행·화면 연결 | `App`, `controller/AppController` (`Launcher`는 패키징 PR #54에 포함) |
| 시작 메뉴·선택·키 매핑 | `screen/StartScreen`, `MenuSelectionModel`, `input/MenuKeyBindings` |
| 모드·난이도 선택 | `screen/ModeSelectionScreen`, `AppController.chooseDifficulty()` |
| 게임 규칙·상태·착지·삭제·점수 반영 | `game/GameEngine`, `GameState`, `GameConfig` |
| 조작·시간 진행 | `game/GameController.handle()` / `advance()` |
| 보드·회전·블록 | `game/Board`, `Tetromino`, `TetrominoType` |
| 점수·생성 확률 | `StandardScoringPolicy`, `Difficulty`, `WeightedPieceGenerator` |
| 표시 데이터·고스트 계산 | `GameSnapshot`, `PieceSnapshot`, `GameSnapshot.ghostBlock()` |
| 게임 그림·NEXT·안내선·배지·연출 | `ui/GameView` |
| 설정·키 변경·저장 | `settings/`, `input/GameKeyBindings` |
| 기록·결과 | `score/ScoreboardManager`, `GameRecord`, `ui/ScoreboardView`, `GameOverView` |
| 색맹 표시·공통 폰트 | `ui/BlockPalette`, `BlockPattern`, `BlockStyle`, `AppFonts` |
| 아이템 선택·부착·벌칙 | `game/item/`, `game/PenaltyController` |

코드 루트는 `app/src/main/java/com/team10/tetris/`, 테스트는 `app/src/test/java/com/team10/tetris/`다.

## 현재 구현 규칙의 요약

- 게임 시작 전 `GameConfig`의 모드·난이도를 확정하고 한 판 도중 난이도는 바꾸지 않는다.
- I 가중치는 Easy 12, Normal 10, Hard 8이며 다른 블록은 각각 10이다. 실제 I 확률은 12/72, 10/70, 8/68이다. 확률 자체의 ±20%와 가중치 ±20%를 혼동하지 않는다.
- 기본 낙하 간격은 느리게/보통/빠르게 1250/1000/750ms. 누적 5줄마다 난이도에 따라 80/100/120ms를 줄이며 최소 200ms다. 기본 속도를 바꿔도 가속 단계를 유지한다.
- 낙하 성공 한 칸은 100점, 실제 간격이 1000ms 미만이면 200점이다. 자동·수동에 공통 적용한다. 줄 삭제는 줄당 1000점과 동시 삭제 추가 줄당 500점이다.
- 기본 조작은 방향키, Space 하드 드롭, ESC 설정/일시정지다. 시작 메뉴 선택은 Enter/Space다. 설정에서 게임 조작 키를 바꿀 수 있다.
- 아이템 모드는 10줄 경계마다 새 NEXT에 선택 가능한 아이템을 지급한다. 이미 보인 NEXT는 유지한다. B/K 예약·진행 중에는 B/K를 제외한다.
- E/B/K/L은 일반 블록 칸에 부착한다. W는 전용 블록으로 교체한다. 상세 발동·점수는 각 기능 명세를 따른다.
- 데모 단축키 1/2/3/4/5는 E/B/K/W/L 예약이다. 데모 ON·아이템 모드에서만 사용한다. 확인 목적으로 원래 지급 상수를 바꾸는 대신 이 기능을 사용한다.
- 고스트는 읽기 전용 보드로 계산하며, 암전·줄 삭제·폭탄 연출·종료 중에는 표시하지 않는다.
- 기록은 모드·난이도별로 구분한다. 설정과 기록의 기본 경로는 사용자 홈 `.team10-tetris`다.

이는 코드와 기존 합의의 정리다. 교수님 요구사항 충족 판정은 원문·테스트·평가 근거를 함께 확인한다.

## 노션의 예전 내용과 달라진 점

| 예전 기록 | 현재 정리 |
| --- | --- |
| 점수 0, `GameView.setScore()` 연결 예정 | 엔진·점수 정책에서 계산하고 스냅샷으로 표시 |
| ESC 단순 일시정지, 설정 화면 연결 예정 | 설정 창과 일시정지·복귀 경로 연결 |
| 키 변경 비활성·색맹만 저장 | 키 변경·설정 저장·초기화 기능 존재 |
| 낙하 간격 전체에 설정 배율 적용 | 기본 간격과 난이도별 감소량 분리 |
| E만 등록, B/K/L 미병합, W 미구현 | 로컬 코드에 E/B/K/L/W 등록 경로 존재 |
| B 3초·K 다음 블록·3% 별도 추첨 | B는 다음 블록 착지까지, K는 자신, 공통 지급 후보에서 선택 |
| 배포 패키지 없음 | PR #54 브랜치에 `packageWindowsZip`·`verifyWindowsPackage` 존재, main 미병합 |

성장 주사·도박 상자·조작 반전·V/G/F는 원문 아이디어로 보존하며 구현된 기능 목록에 넣지 않는다. 법인차량 시스템과 지도 서비스 토론 과제는 테트리스 코드 설계·팀 스프린트와 구분한다.

## 배포

아래 명령과 배포 문서는 **PR #54의 `feature/windows-package` 브랜치에 있는 구현**이다. main에서는 해당 PR 병합 전까지 사용할 수 없다.

```powershell
.\gradlew.bat :app:packageWindowsZip
.\gradlew.bat :app:verifyWindowsPackage
```

산출물과 검증 범위는 [PR #54의 배포 문서](https://github.com/tetris-swe-team10/tetris/blob/feature/windows-package/docs/distribution.md)를 참고한다. ZIP 생성·로컬 실행 확인과 GitHub Release 업로드는 다른 작업이며 실제 업로드 전에는 배포 완료라고 적지 않는다.
