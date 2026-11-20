# A · 평가 시연과 실제 플레이 점검

> 작성 2026-11-17 · 담당 A 혜원 · 상태: 절차 준비·자동 검증 기록(2026-11-20), 이 문서의 수동 시연은 아직 미실행.
> 근거: 교수님 Requirements 3의 3~6쪽, [A 작업](A-local-versus.md), 사용자 승인 D-009. P2P·디자인·공용 렌더러 구현은 B/C 전담.

## 준비

1. 시연할 커밋과 운영체제를 기록하고 실행한다: `./gradlew.bat :app:run`.
2. 기존 점수·키 설정은 삭제하지 않는다. 기본 키를 바꿨다면 현재 키 안내를 보고 진행한다. 테스트용 새 이름을 저장할 필요는 없다.
3. 기본 조작: 1P A/D/W/S/Space, 2P 방향키/Enter, ESC 양쪽 정지. 같은 PC 대전은 시작 전에 모드와 공통 난이도를 고른다.
4. 아래 표에 실제 실행 결과와 캡처/영상 위치를 적는다. 구현·자동 테스트·실제 두 사람 플레이는 별도 근거다.

## 실제 플레이 순서

| 순서 / 요구사항 | 조작과 확인할 결과 | 현재 실행 상태 |
| --- | --- | --- |
| 1 / V-1 | 시작 메뉴 → 같은 PC 대전 → 일반/아이템/시간제한을 각각 선택. 시작 전 공통 난이도 선택, 진행 중 난이도 변경 항목 없음 확인 | 미실행 |
| 2 / V-2 | 두 사람이 동시에 좌우·낙하. 상대 보드 대신 자기 보드만 움직임. 키를 함께 길게 눌러도 양쪽 이동 유지. 창/전체/세로에서 두 보드·NEXT·점수 확인 | 미실행 |
| 3 / V-3 | 일반 대전에서 한 줄 삭제는 공격 없음. 두 줄 이상 자연 완성 시 상대 대기 영역에 같은 줄 수 표시. 마지막 블록 자리만 구멍인지 삭제 직전 화면과 비교 | 미실행 |
| 4 / V-4·V-5 | 받은 공격은 현재 블록 조작 중에는 대기에 머묾. 현재 블록 처리·삭제 연출 후 다음 블록 생성 전에 아래쪽 회색 줄로 삽입. 위에 쌓인 칸도 함께 이동 | 미실행 |
| 5 / V-6 | 공격을 여러 번 받아 기존 줄 아래에 새 줄이 붙는지 확인. 10줄·초과 절단의 정확한 모양은 아래 결정적 테스트로 증명하고 수동 재현을 했다고 대신 기록하지 않음 | 미실행 |
| 6 / V-7 | 한쪽이 블록을 더 쌓지 못하면 상대 승리 표시. 종료 뒤 게임 조작으로 점수/보드가 바뀌지 않음. 재시작은 새 판, 메인 메뉴는 시작 화면으로 복귀 | 미실행 |
| 7 / V-8 | 대전 전후 스코어보드의 해당 그룹이 그대로인지 확인. 점수 초기화는 누르지 않음. 기존 `~/.team10-tetris/scores.tsv`가 있다면 전후 해시 비교, 없다면 대전으로 새 파일이 생기지 않는지 확인 | 미실행 |
| 8 / V-9 | 아이템 대전에서 아이템 지급·표시·효과를 확인. B/K는 자신에게 적용. E/L/W 삭제는 공격 제외, 일반 블록의 자연 완성 2줄 이상은 공격. 무작위라 관찰하지 못한 종류는 미확인으로 남김 | 미실행 |
| 9 / V-10 | 시간제한 시작 시 120초. ESC에서 양쪽 보드와 타이머 정지, 복귀 후 진행. 만료 시 높은 점수 승리, 동점 무승부. 일반/아이템 대전에 제한 시간이 잘못 표시되지 않음 | 미실행 |
| 10 / 2인 키 | 키 설정에서 다른 플레이어가 쓰는 키·ESC 입력 거부. 사용하지 않는 키로 변경·저장 후 프로그램 재실행 시 유지. 재현 후 원래 키로 복원·저장 | 미실행 |

개발 중 증거로 사용하는 파일은 개인 이름·홈 경로 등 불필요한 정보를 포함하지 않도록 확인한다.
프레임 저하·키보드 동시 입력 누락·표시 이상은 입력 순서, 모드, 난이도, 발생 시점과 함께 A에게 기록한다. B/C 영역 문제는 코드를 대신 고치지 않고 요청서로 전달한다.

## 무작위 플레이 대신 확인할 결정적 경계 테스트

아래는 미리 정한 보드와 블록을 넣는 자동 테스트다. 시연용 숨은 단축키나 새 게임 기능을 추가할 필요가 없다.

| 조건 | 기존 테스트 메서드 (`LocalVersusMatchTest`) |
| --- | --- |
| 대기 8줄에 4줄 수신 → 10줄까지 유지, 추가 수신 무시 | `pendingQueueKeepsOldThenTopOfNewAndIgnoresWhenFull` |
| 2줄의 마지막 블록 자리 제외, 300ms 삭제 전/후 전달 시점 | `doubleClearSendsHolesFromLastPieceOnlyAfterAnimation` |
| 1줄 삭제는 무공격 | `singleClearDoesNotAttack` |
| 같은 시각 양쪽 삭제의 호출 순서 차이 방지 | `simultaneousClearsDeliverAfterBothUpdatesWithoutPlayerOrderAdvantage` |
| 회색 식별의 압축·파괴와 동기화 | `garbageIdentityMovesWithRowCompressionAndDisappearsWhenDestroyed` |
| 상단 넘침 및 다음 생성 자리 막힘 패배 | `overflowLosesAtNextSpawnAndFreezesMatch`, `shiftedStackBlockingNextPieceAlsoEndsMatch` |
| 일시정지 시간 제외·정확한 만료·동점 무승부 | `timerStopsForPauseExpiresExactlyAndAllowsDraw` |
| 시간 만료 시 고득점 승리, 아이템 모드 설정 | `higherScoreWinsAndItemModeUsesItems` |
| L 삭제의 점수는 유지, 공격은 제외 | `lineItemClearsAndScoresButDoesNotSendAttack` |

```powershell
.\gradlew.bat :app:test --tests '*LocalVersusMatchTest' --tests '*VersusKeyBindingsTest' --offline
.\gradlew.bat :app:uiTest --tests '*LocalVersusViewTest' --offline
```

위 명령은 필요할 때만 실행한다. 기존 UI 테스트는 입력·일시정지·회색 픽셀·세로 배치를 확인하지만 실제 두 사람의 키보드 사용과 장시간 플레이를 전부 대신하지 않는다.
V-8의 실제 스코어 파일 전후 확인과 재시작/메인 복귀 수동 결과도 자동 테스트 통과로 대체하지 않는다.

## 자동 검증·배포본 기록

수동 시연 결과가 아니다. 위 실제 플레이 표의 상태는 바꾸지 않는다.

| 날짜 | 기준 | 명령 | 결과 |
| --- | --- | --- | --- |
| 2026-11-20 | main `0b1ffc4`, Windows 11, JDK 21 | `:app:test --tests '*LocalVersusMatchTest' --tests '*VersusKeyBindingsTest'`, `:app:uiTest --tests '*LocalVersusViewTest'` | 3개 클래스 12개 테스트 통과. 위 결정적 경계 테스트 표의 메서드 전부 포함 |
| 2026-11-20 | main `0b1ffc4`, Windows 11, JDK 21 | `:app:verifyWindowsPackage` | 통과. 압축 해제 94.25MB(500MB 이내), EXE 아이콘·전용 런타임 포함, 실행·정상 종료 확인 |

선별 실행이며 전체 테스트·커버리지 측정이 아니다. 열린 PR(#70 배포·입력 검증, #71 렌더러 통합)이 병합되면 최종 커밋에서 다시 실행한다.

## 실행 후 남길 기록

```text
실행일 / 실행자 2명:
커밋 / OS / 화면 모드 / 키보드:
대전 모드 / 난이도:
확인한 표의 번호:
결과: 통과 / 실패 / 미확인
스크린샷·영상 또는 테스트 보고서:
문제 재현 입력·기대 결과·실제 결과:
후속: A 수정 / B 요청 / C 요청 / 팀 규칙 확인
```

## 평가 시 설명할 핵심 코드

- `GameEngine`: 자연 완성 공격 줄 생성, 받은 줄 10줄 대기, 생성 직전 삽입.
- `Board.insertGarbage`: 순서대로 아래 삽입, 상단 넘침 확인. 셀 값으로 회색 식별을 함께 이동.
- `LocalVersusMatch`: 두 엔진·같은 시각 공격 전달·양쪽 일시정지·승패·시간제한.
- `LocalVersusView` / `VersusKeyBindings`: 두 보드와 키 분배·독립 반복 입력·키 저장/충돌 검사.
- 아이템 세부 근거는 [요구사항·테스트 추적표](../item-requirements.md)를 사용한다.
