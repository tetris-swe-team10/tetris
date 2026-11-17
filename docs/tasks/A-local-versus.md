# A · 혜원 · 같은 PC 대전

목표: V-1~V-10 전체 흐름과 N-3 아이템 요구사항 명세서를 책임진다.
브랜치: `feature/local-versus`. B/C의 기능까지 구현 완료로 표시하지 않는다.

## 요구사항과 할 일

- [x] V-1 시작 메뉴 → 일반/아이템/시간제한 → 공통 난이도 선택
- [x] V-2 두 엔진·두 보드와 독립 키 입력
- [x] V-3 자연 완성 2줄 이상 삭제 → 마지막 블록 칸을 빈칸으로 보낸다
- [x] V-4 받은 줄 표시 → 현재 블록 처리 후 다음 블록 생성 직전 삽입
- [x] V-5 회색 칸 식별, 압축·아이템 파괴 후 식별도 함께 갱신
- [x] V-6 10줄 대기 제한, 기존 줄 우선, 새 줄 아래 추가·넘친 아래쪽 절단
- [x] V-7 상단 넘침·생성 불가 판정 → 상대 승리, 종료 이후 조작 차단
- [x] V-8 대전 결과는 ScoreboardManager에 저장하지 않음
- [x] V-9 기존 E/B/K/L/W 사용, B/K 자신에게 적용
- [x] V-10 120초·점수 비교·무승부·양쪽 일시정지
- [x] 2인 키 변경·저장·전체 충돌 검사, 재시작·메인 복귀
- [x] N-3 [아이템 요구사항 명세](../item-requirements.md)
- [ ] B 리팩토링 PR 리뷰(실제 PR 확인 후 기록)

## 코드와 연동

| 범위 | 파일/계약 |
| --- | --- |
| 공격 | `GameEngine.receiveAttack`, `AttackListener`, `AttackLines`, `Board.insertGarbage` |
| 한 판 | `LocalVersusMatch implements VersusMatch` |
| 화면·키 | `LocalVersusView`, `VersusKeyBindings`, `AppController` |
| 그리기 | `BoardRenderer` 주입. B 구현 전 `VersusBoardRenderer` 사용 |
| C에게 제공 | 공격 콜백·수신 진입점, `GameSnapshot.garbageCells/pendingAttack` |

2인 키는 1인 키 설정을 덮어쓰지 않고 `~/.team10-tetris/versus-keys.properties`에 저장한다.
기본값: 1P A/D/W/S/Space, 2P 방향키/Enter. ESC 양쪽 일시정지.

## 검증

2026-11-16 로컬 선별 검증 통과: 대전 엔진·키·인터페이스 직렬화, 기존 보드·엔진·폭탄 연출·L·W 직접 영향 범위, 대전 UI·시작 메뉴 레이아웃.
공용 엔진에 추가한 수신 로직 때문에 기존 ‘빈 수신 함수’ 전제만 제거하고, 실제 수신 후 회색 칸·대기 줄 직렬화 검증을 추가했다.
전체 회귀 테스트·공식 커버리지 측정·동료 리뷰·main 병합은 수행하지 않았다. Issue [#58](https://github.com/tetris-swe-team10/tetris/issues/58)에서 PR과 요청 전달을 연결한다.

```powershell
.\gradlew.bat :app:test --tests '*LocalVersusMatchTest' --tests '*VersusKeyBindingsTest' --tests '*VersusInterfaceTypesTest' --offline
.\gradlew.bat :app:uiTest --tests '*LocalVersusViewTest' --tests '*StartScreenLayoutTest' --offline
```

직접 영향 검증에 사용한 추가 선택자: `*GameEngineTest`, `*BoardTest`, `*BombFlashTest`, `*LineClearItemTest`, `*WeightItemTest`.
화면 확인 이미지: `app/build/ui-smoke/local-versus.png` (로컬 산출물, Git 제외).

`LocalVersusMatchTest`: 무공격·다중 줄 구멍·삭제 연출 시점·대기 10줄 경계·회색 압축·넘침·타이머·무승부·승패.
`VersusKeyBindingsTest`: 두 사람 매핑·충돌 거부·저장/불러오기·손상 파일 복구.
화면·렌더러는 별도 UI 테스트로 검증한다. B/C 미완성 기능과 LAN 실측은 이 PR 완료 범위가 아니다.

## 후속 연동

- [ ] C의 확정 목업 반영 (현재는 동작 확인용 화면)
- [ ] B 공용 렌더러 병합 후 생성 시 주입 구현 교체, 임시 렌더러 제거 검토
- [ ] 이슈/PR 번호·동료 리뷰·원격 병합 근거 기록

게시: Issue #58 / [PR #59](https://github.com/tetris-swe-team10/tetris/pull/59). 커밋·push 및 B/C 리뷰 요청 전달 완료. 동료 코멘트·수정 답글·main 병합은 대기.
