# 품질·평가 근거 관리

> 정리일: 2026-11-09 · 이번 문서 작성에서 게임 테스트·커버리지·배포 검증은 다시 실행하지 않음

## 1. 요구사항부터 증거까지 연결

`요구사항 ID/원문 위치 → 담당 작업·Issue → 구현 파일 → PR·리뷰 → 테스트 명령·결과/보고서`를 연결한다.
노션 2차 분석의 `R2-페이지-번호`와 평가 체크리스트의 `2-18` 같은 번호는 다른 체계다. 대응을 확인하기 전에는 같은 번호라고 쓰지 않는다.

| 검증 대상 | 구현 근거 | 자동 검증 후보 | 현재 정리의 한계 |
| --- | --- | --- | --- |
| 낙하·줄 삭제 점수·동시 삭제 보너스 | `StandardScoringPolicy`, `GameEngine` | `ScoringDifficultyTest` | 이번 작업 미실행 |
| 난이도별 I 확률·추첨 구간 | `Difficulty`, `WeightedPieceGenerator` | `ScoringDifficultyTest.actualGameGeneratorMatchesAllDifficultyDistributions`, `weightedSelectionUsesEveryIntervalBoundary` | 기대값이 `Difficulty.weight()`에서 나옴. 가중치 12/10/8 자체의 명세 일치도 별도 확인 |
| 기본 속도와 5줄 가속 | `DropSpeed`, `GameEngine` | `DropSpeedSettingsTest` | 설정/난이도 조합·최소 간격·점수 연동 확인 |
| 모드·난이도별 기록 | `ScoreboardManager` | `ScoreboardGroupsTest`, `ScoreboardManagerTest` | 저장 파일의 호환·오류 경로도 포함 |
| E / L / W | `game/item/`, `GameEngine` | `SmallBombTest`, `BombFlashTest`, `LineClearItemTest`, `WeightItemTest` | 아이템 간 연동은 `ItemIntegrationTest`에서 관련 사례 선택 |
| B / K | `Blackout`, `RotationLock`, `PenaltyController` | `BlackoutTest`, `RotationLockTest`, 관련 ViewTest | 예약·활성·종료·일시정지·고스트를 함께 고려 |
| 고스트·범위 안내 | `GameSnapshot`, `GameView` | `GhostBlockTest`, `GhostBlockViewTest` | 암전과 상태 조건을 독립 검증 |
| 설정·키 변경·저장·복구 | `settings/`, `GameKeyBindings` | `SettingsPersistenceTest`, `SettingsAdditionsUiTest`, `GameKeyBindingsTest` | 모델 계산과 실제 화면 반영은 별도 경로 |
| 화면 크기 대응·사용성 | 화면 layout | `StartScreenLayoutTest`, `ModeSelectionLayoutTest`, `SettingsGameLayoutTest` | 비기능 요구사항과 연결 가능. CPU/RAM 증거로 사용 불가 |
| 색맹 표시·폰트 일관성 | `BlockPalette`, `AppFonts` | `BlockPaletteTest`, `AppFontsViewTest` | 모든 OS의 동일 렌더링을 보장하는 시험은 아님 |
| 아이콘·실행 패키지·배포물 용량 | Gradle packaging, `tools/verify-windows-package.ps1` | `:app:verifyWindowsPackage` | Windows 데스크톱 필요. 전체 CPU/RAM 최소 사양 검증은 아님 |
| CPU·RAM 최소 사양, 입력 3회/초·1초 미만 표시 | 환경 제한과 측정 계획 필요 | 전용 검증 근거 추가 필요 | 노션 중간 점검에서 미비로 기록. 현재 통과 증거를 이번 작업에서 확인하지 않음 |
| 이슈·PR·리뷰·기여도 | GitHub와 작업 기록 | PR/Issue/리뷰 URL, 스프린트 기록 | 원격 최신 현황 전수 감사는 이번 범위가 아님 |

이 표는 근거를 찾는 지도이며 일괄 통과 판정표가 아니다. 테스트가 존재하는 것과 실행해서 통과한 것은 다르다.

## 2. 일상 작업은 선별 실행

```powershell
# 수정한 메뉴 키 매핑만
.\gradlew.bat :app:test --tests "*MenuKeyBindingsTest"

# 확률 생성의 실제 분포와 모든 추첨 구간
.\gradlew.bat :app:test --tests "*ScoringDifficultyTest.actualGameGeneratorMatchesAllDifficultyDistributions" --tests "*ScoringDifficultyTest.weightedSelectionUsesEveryIntervalBoundary"

# 고스트 로직과 직접 관련된 UI만
.\gradlew.bat :app:test --tests "*GhostBlockTest" :app:uiTest --tests "*GhostBlockViewTest"

# 줄 삭제 아이템의 로직만
.\gradlew.bat :app:test --tests "*LineClearItemTest"
```

`--tests`는 바로 앞의 Test task에 적용한다. JavaFX 테스트는 `uiTest`, 일반 로직은 `test`로 실행한다. 의존성이 이미 준비된 경우에만 `--offline`을 덧붙인다.
관련 범위가 바뀌거나 문제가 발생했을 때 검증을 넓힌다. 문서만 수정한 경우 게임 테스트를 실행하지 않고 링크·경로·인용과 원문 보존을 확인한다.

## 3. 커버리지 측정과 수치의 의미

첨부 노션 「2차 요구사항 분석」 12쪽 정리에는 중간 **Line Coverage 50%**, 기말 **70%**가 기록되어 있다. 이는 노션 분석서에서 가져온 기준이며 최신 교수님 원문·e-class 공지가 바뀌면 그 근거와 함께 갱신한다.

현재 `app/build.gradle`은 JaCoCo의 LINE 보고서를 생성하고 `test.exec`와 `uiTest.exec`를 합친다. GUI를 제외하는 설정이나 커버리지 미달 시 빌드를 실패시키는 임계값 task는 현재 없다. 문서의 50/70은 자동으로 강제되는 설정이라고 설명하지 않는다.

- 일상 PR: 해당 테스트의 통과/실패를 보고한다. 선별 실행 후 전체 커버리지의 상승·하락을 공식 수치로 발표하지 않는다.
- 평가·릴리스: 필요한 시점에 같은 커밋의 전체 테스트를 한 번 측정한다. 보존할 패키지·작업물을 지우지 않도록 별도 검증 폴더 사용을 권장한다.
- 이전 실행의 `test.exec`와 새 코드의 `uiTest.exec`를 섞지 않는다. 두 task를 모두 다시 실행해 같은 버전의 데이터를 만든다.

```powershell
# 전체 측정이 필요한 평가/릴리스 시점에만 실행
.\gradlew.bat :app:test :app:uiTest :app:jacocoTestReport --rerun-tasks
```

| 결과 | 위치 |
| --- | --- |
| 일반 테스트 HTML / XML | `app/build/reports/tests/test/index.html`, `app/build/test-results/test/` |
| UI 테스트 HTML / XML | `app/build/reports/tests/uiTest/index.html`, `app/build/test-results/uiTest/` |
| 전체 LINE HTML / XML | `app/build/reports/jacoco/test/html/index.html`, `app/build/reports/jacoco/test/jacocoTestReport.xml` |
| UI 확인 PNG | `app/build/ui-smoke/` |
| Windows 패키지 자동 검증 JSON | `app/build/reports/packaging/windows.json` |

커버리지는 `covered / (covered + missed) × 100`으로 계산하고 분자·분모·대상 코드·제외 범위·커밋·환경·명령·날짜를 같이 기록한다. 과거 노션의 70.00%·70.65%·테스트 개수를 오늘의 결과로 옮기지 않는다. 숫자를 높이려고 GUI나 어려운 코드를 임의로 제외하지 않는다.

## 4. 검증 기록 양식

```text
요구사항/이슈:
기준 커밋 및 미커밋 변경 포함 여부:
환경: OS, JDK, GUI 가능 여부
변경·검증 범위:
실행 명령:
결과: 통과/실패/건너뜀/미실행 및 개수
전체 실행 여부와 이유:
LINE: 전체 측정 시에만 covered/missed/비율
보고서·화면·PR 링크:
미확인 항목 및 다음 담당자:
```

프로세스 메모리 측정은 OS를 포함한 1GB RAM 환경의 실행 증거가 아니다. 폴더 용량 검사는 파일 크기 합이고 파일 시스템 할당 크기·설치 중 임시 공간과 다를 수 있다. 최소 사양을 주장하려면 환경과 측정 범위를 정확하게 적는다.

## 5. 리뷰·스프린트·제출 증빙

- PR에는 연결 이슈, 작성자, 변경 범위, 테스트 명령과 실제 결과를 남긴다. 동료는 구체적인 코드 근거로 리뷰하고 작성자는 반영 커밋·답글을 연결한다.
- 사후 리뷰와 작성자 자체 점검은 각각 표시한다. 평가를 위해 병합 전 동료 리뷰가 있었던 것처럼 기록하지 않는다.
- 스프린트에는 사용자 스토리, 책임자, 완료 조건, 실제 이슈·PR, 미완료/이월 사유를 기록한다. 커밋 수만으로 기여도를 설명하지 않는다.
- 제출 시 평가표의 각 항목에서 코드·테스트·명세·배포물·이슈/PR까지 이동 가능한지 확인한다. Release 업로드가 없다면 로컬 ZIP 존재만으로 원격 배포 완료라고 쓰지 않는다.
