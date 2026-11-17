# 2차 아이템 요구사항 명세

> 갱신 2026-11-17 · N-3 / A 혜원. 기존 구현과 기능별 명세를 정리한 평가용 문서이며 교수님 원문과 팀이 추가한 규칙을 구분한다. 아래 테스트는 현재 존재하는 검증 근거이며 문서 작성 중 새로 전체 실행했다는 뜻이 아니다.

## 공통 요구사항

| ID | 요구사항 | 검증 기준 |
| --- | --- | --- |
| IT-01 | 아이템 모드에서 누적 10줄마다 새로 보충하는 NEXT에 공통 목록 중 아이템을 지급한다. 기존 NEXT는 바꾸지 않는다. | 아이템 할당·줄 삭제 후 NEXT 테스트 |
| IT-02 | 기본 블록에 붙는 아이템은 점유 칸 중 무작위 한 칸에 문자로 표시한다. W는 전용 블록이다. | 할당 위치와 NEXT/현재 블록 표시 확인 |
| IT-03 | 일시정지는 삭제·아이템 연출도 정지하고, 재시작·게임 종료 시 지속 효과를 초기화한다. | 엔진 상태 전이 테스트 |
| IT-04 | 데모 기능은 일반 지급 규칙과 구분하며 설정 OFF 시 일반 규칙으로 복귀한다. | 데모 설정·간격 변경 테스트 |

## 아이템별 동작과 완료 조건

| ID / 문자 | 종류 | 트리거·효과 | 점수·종료 조건 |
| --- | --- | --- | --- |
| IT-E / E | 팀 추가: 작은 폭탄 | 붙은 블록 고정 → 아이템 칸 중심 3×3의 유효 칸을 300ms 동안 75ms 단위로 깜빡인 뒤 제거 | 폭발 칸은 점수·줄 수 없음. 남은 완성 줄만 기존 채점. 개별 칸 중력 없음 |
| IT-B / B | 팀 추가: 암전 | 고정·삭제 연출 후 다음 블록에 적용. 점유 영역 중심을 따라 지름 4칸 원형 시야 이동 | 현재 블록 전체·NEXT·점수 유지, 고스트 숨김. 대상 블록 착지 때 종료. B/K 예약·대기·진행 중에는 B/K 모두 새 선택 제외 |
| IT-K / K | 팀 추가: 회전 잠금 | K가 붙은 블록 자체가 생성될 때 회전을 금지 | 좌우·낙하는 유지하고 해당 블록 착지 때 종료. B/K 선택 가능 여부는 기존 정책 유지 |
| IT-L / L | 교수님 제시: 줄 삭제 | 기본 블록 내 무작위 점유 칸에 L. 고정 시 L이 있는 행을 미완성이라도 삭제 | 자연 완성 행과 합집합으로 한 번씩 삭제, 기존 줄 점수 적용 |
| IT-W / W | 교수님 제시: 무게추 + 기존 팀 세부 규칙 | 전용 모양 `.WW./WWWW`, 회전 불가. 충돌 후 좌우 잠금, 폭 4칸을 파괴하며 바닥까지 진행 | 파괴 중 점수·줄 수·압축 없음. 바닥에서 사라지고 다음 블록으로 진행 |

## 같은 PC 대전의 추가 규칙

사용자 승인(2026-11-16): E/L/W로 지운 줄은 공격을 만들지 않는다. 일반 블록 또는 B/K 블록으로 자연 완성한 줄은 2줄 이상일 때 공격한다.
B/K는 자신에게 적용하며 상대에게 벌칙을 전송하지 않는다. 대전 결과를 스코어보드에 저장하지 않는다.

## 구현·검증 위치

`game/item/*`, `PenaltyController`, `GameEngine`, `RandomItemAssigner`를 사용한다.
상세 경계와 화면 흐름은 [작은 폭탄](small-bomb.md), [암전](blackout.md), [문서 지도](README.md)의 아이템 명세를 함께 읽는다.
이 문서 작성만으로 모든 아이템 회귀 검증·교수님 인정·커버리지 목표를 달성했다고 표시하지 않는다.

## 요구사항별 완료 조건과 기존 테스트 연결

테스트 파일은 `app/src/test/java/com/team10/tetris/game/` 기준이다. 클래스와 메서드를 함께 적어 검증할 조건을 구분한다.

| ID | 관찰 가능한 완료 조건·예외 | 근거 테스트 |
| --- | --- | --- |
| IT-01 | 9줄에는 미지급, 10줄 경계에서 기존 NEXT가 현재로 승격되고 새 NEXT에 지급. 초과 줄은 다음 경계에 이월. 일반 모드는 미지급 | [SmallBombTest](../app/src/test/java/com/team10/tetris/game/SmallBombTest.java): `tenLineBoundaryDecoratesOnlyNewNextAndPreviewSurvivesPromotion`, `overflowKeepsLineRemainderAndAwardsAtEachNewBoundaryOnly`, `normalModeNeverUsesAssignerAndRestartResetsRewardProgress` |
| IT-02 | 모든 도형의 실제 점유 칸만 부착 후보. 회전·실패한 회전 복원 후에도 마커 위치 일치. W는 전용 도형으로 교체 | `SmallBombTest.randomAttachmentCanChooseEveryRealCellOfEveryShape`, `rejectedRotationRestoresMarkerAndMovementKeepsRelativePosition`; [WeightItemTest](../app/src/test/java/com/team10/tetris/game/WeightItemTest.java)의 `assignerAttachesOrReplacesAndKeepsLambdaCompatibility` |
| IT-03 | 일시정지 중 삭제·폭발·암전·회전 잠금·무게추가 임의로 진행/해제되지 않음. 재시작·게임 오버 시 효과 잔류 없음 | [BombFlashTest](../app/src/test/java/com/team10/tetris/game/BombFlashTest.java): `pauseFreezesBothFlashPhaseAndRemovalDeadline`, `blockedSpawnBecomesGameOverOnlyAfterTheFlashAndRestartHasNoEffect`; `BlackoutTest.pausePreservesEffectAndSoftLandingEndsIt`; `RotationLockTest.clearAndGameOverDoNotLeakLock`; `WeightItemTest.pauseStopsBothMovementAndDrilling` |
| IT-04 | 데모 ON/OFF·간격 변경·예약 지급이 서로 구분되고 일반 규칙으로 복귀 | [DemoItemTest](../app/src/test/java/com/team10/tetris/game/DemoItemTest.java)의 `intervalChangeStartsFreshAndExcessAwardsAreQueued`; [DemoShortcutTest](../app/src/test/java/com/team10/tetris/game/DemoShortcutTest.java)의 `disableCancelsPendingAndPauseRejectsInput`; `SettingsInteractionTest.keyboardRowsOptionsDemoControlsAndPersistence`. 수동 단축키는 기존 1인 아이템 화면에서 확인하며 A 대전 화면에 같은 단축키가 있다고 가정하지 않음 |
| IT-E | 고정 전 미발동, 보드 밖 좌표는 자름. 300ms 전에는 칸 유지, 75ms 단위 강조 전환. 폭발 칸은 무득점·무중력, 남은 완성 행만 별도 채점 | `SmallBombTest.allCornersAndEdgesClipToBoardAndEmptyBoardIsSafe`, `bombOnLockedPieceExplodesOnceWithoutPointsOrLineCredits`; `BombFlashTest.whiteOverlayBlinksTwiceInThreeHundredMilliseconds`, `survivingFullRowStartsItsOwnAnimationAfterExplosionAndScoresOnce` |
| IT-B | B 착지·삭제 이후 다음 블록에 발동. 일시정지 유지, 대상 착지 종료, 숨긴 블록도 충돌 판정 유지 | [BlackoutTest](../app/src/test/java/com/team10/tetris/game/BlackoutTest.java): `previewsThenLastsForExactlyOneFollowingPiece`, `clearDefersActivationAndTargetClearEndsEffectImmediately`, `hiddenBoardStillCollidesAndGameOverClearsEffect`; [BlackoutViewTest](../app/src/test/java/com/team10/tetris/ui/BlackoutViewTest.java)에서 원 안/밖 실제 표시 확인 |
| IT-K | 이전 블록은 회전 가능하고 K 자체는 불가. 좌우·낙하 유지, 착지 후 다음 블록은 정상. B/K 중 하나가 예약되어도 둘 다 추첨 제외 | [RotationLockTest](../app/src/test/java/com/team10/tetris/game/RotationLockTest.java): `nextDoesNotLockPreviousPieceButPromotionLocksKItself`, `movementDropsAndPauseWorkAndNextPieceCanRotate`, `bothPenaltiesAreExcludedWhileEitherIsReservedOrActive` |
| IT-L | 미완성 행도 삭제. 자연 완성 행과 합집합으로 압축·채점하며 같은 행은 한 번만 계산 | [LineClearItemTest](../app/src/test/java/com/team10/tetris/game/LineClearItemTest.java): `partialRowIsClearedWithAnimationAndNormalScore`, `itemRowAndFullRowAreClearedTogetherWithComboScore`, `itemOnAlreadyFullRowCountsOnce`, `attachPositionCanBeEveryCellOfIBlock` |
| IT-W | `.WW./WWWW` 고정 모양·회전 불가, 접촉 후 좌우 금지. 파괴 중 점수·줄 수·압축 없음, 바닥 소멸. ON/OFF 지급 규칙과 무관하게 무게추의 수명 일치 | `WeightItemTest.fixedShapeSymbolAndNoRotation`, `contactLocksSidesAndDrillsOneRowPerTickWithoutPoints`, `emptyBoardReachesFloorAndVanishesWithoutLocking`, `hardDropFromTheTopFallsThenDrillsToTheFloor` |

## 같은 PC 대전의 검증 범위

[LocalVersusMatchTest](../app/src/test/java/com/team10/tetris/game/versus/LocalVersusMatchTest.java)의 `lineItemClearsAndScoresButDoesNotSendAttack`은 **L로 만든 삭제의 무공격**을 직접 검증한다.
`higherScoreWinsAndItemModeUsesItems`는 아이템 모드 선택과 시간제한 승패를 검증한다. 이 두 테스트만으로 대전에서 E/B/K/W의 모든 조합을 검증했다고 표현하지 않는다.
기존 아이템 엔진 테스트와 [A 시연 절차](tasks/A-evaluation.md)의 실제 아이템 대전 점검을 함께 사용한다.

## 필요한 부분만 실행하는 예

```powershell
# 폭탄 규칙과 연출을 수정했을 때
.\gradlew.bat :app:test --tests '*SmallBombTest' --tests '*BombFlashTest' --offline
# 대전 공격 규칙을 수정했을 때
.\gradlew.bat :app:test --tests '*LocalVersusMatchTest' --offline
```

문서 링크·테스트 이름만 정리할 때는 게임 테스트를 반복하지 않는다. 최종 공식 Line Coverage 70%는 B 측정 결과를 받아 A 담당 부족 영역만 보강한다.
