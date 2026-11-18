# 역할별 작업 시작점

> 2026-11-16 · A 혜원 / B 수연 / C 서현. 배정 출처: [첨부 노션 출처 기록](source-req3-plan.md).
> 교수님 「Requirements 3」 PDF와 팀 결정은 구분한다. 원문 PDF 3~6쪽: 같은 PC 대전, 8~11쪽: P2P, 13~14쪽: 평가·주의사항.

| 내 역할 | 먼저 읽을 문서 | 책임지는 결과 |
| --- | --- | --- |
| A 혜원 | [A 할 일](A-local-versus.md) | 같은 PC에서 두 명이 실제로 대전하고 승패 확인 |
| B 수연 | [B 할 일](B-network-lobby.md) | 공용 렌더러·입력 리팩토링, 두 PC 연결·대기실 |
| C 서현 | [C 할 일](C-design-p2p-game.md) | 화면 디자인, 연결된 두 PC의 게임 진행·복귀 |

최종 평가 대비 전체 점검은 [시연 체크리스트](demo-checklist.md)를 본다.

## 역할 배분 이유

> 2026-11-18 A/Claude 정리. 노션 「3차 요구사항 역할 분담 · 2주 계획」(정수연 작성)의 배정을 1·2차 이슈 이력과 연결했다. 노션 원문에는 배분 이유 문장이 없으므로, 아래 이유는 이력에서 정리한 설명이다. B·C는 자기 항목이 사실과 다르면 고친다.

| 역할 | 1·2차에 맡았던 일 (역할 라벨 이슈) | 3차 배정 | 연결 이유 |
| --- | --- | --- | --- |
| A 혜원 | 보드·충돌, 이동·회전, 줄 삭제, 생성·낙하, 게임 화면, 점수·난이도, 작은 폭탄 E ([#1](https://github.com/tetris-swe-team10/tetris/issues/1), [#2](https://github.com/tetris-swe-team10/tetris/issues/2), [#3](https://github.com/tetris-swe-team10/tetris/issues/3), [#4](https://github.com/tetris-swe-team10/tetris/issues/4), [#5](https://github.com/tetris-swe-team10/tetris/issues/5), [#14](https://github.com/tetris-swe-team10/tetris/issues/14), [#19](https://github.com/tetris-swe-team10/tetris/issues/19), [#30](https://github.com/tetris-swe-team10/tetris/issues/30), [#36](https://github.com/tetris-swe-team10/tetris/issues/36)) | 같은 PC 대전 규칙 엔진·화면, 2차 아이템 명세서 | 대전 공격은 `GameEngine`의 줄 삭제·블록 생성 시점에 붙는다. 엔진을 만든 사람이 규칙을 넣어 기존 1인 게임 회귀를 줄인다. |
| B 수연 | 색맹 모드, 게임 종료 화면, 스코어보드 저장, clone 후 빌드 오류 수정, 줄 삭제 L, 무게추 W ([#7](https://github.com/tetris-swe-team10/tetris/issues/7), [#8](https://github.com/tetris-swe-team10/tetris/issues/8), [#9](https://github.com/tetris-swe-team10/tetris/issues/9), [#10](https://github.com/tetris-swe-team10/tetris/issues/10), [#38](https://github.com/tetris-swe-team10/tetris/issues/38), [#51](https://github.com/tetris-swe-team10/tetris/issues/51), [#53](https://github.com/tetris-swe-team10/tetris/issues/53)) | 커버리지 측정·공용 렌더러 리팩토링, P2P 연결·대기실 | 빌드 환경 문제를 해결하고 파일 저장·화면 전환을 다뤘다. 측정·리팩토링과 연결 전 단계(메뉴→대기실)를 맡는다. |
| C 서현 | 시작 메뉴, 설정 화면·일시정지, Figma 설정 UI, 반응형 배치, 모드 선택, 암전 B, 회전 잠금 K ([#11](https://github.com/tetris-swe-team10/tetris/issues/11), [#12](https://github.com/tetris-swe-team10/tetris/issues/12), [#13](https://github.com/tetris-swe-team10/tetris/issues/13), [#18](https://github.com/tetris-swe-team10/tetris/issues/18), [#20](https://github.com/tetris-swe-team10/tetris/issues/20), [#23](https://github.com/tetris-swe-team10/tetris/issues/23), [#24](https://github.com/tetris-swe-team10/tetris/issues/24), [#25](https://github.com/tetris-swe-team10/tetris/issues/25), [#32](https://github.com/tetris-swe-team10/tetris/issues/32), [#34](https://github.com/tetris-swe-team10/tetris/issues/34), [#40](https://github.com/tetris-swe-team10/tetris/issues/40), [#42](https://github.com/tetris-swe-team10/tetris/issues/42), [#48](https://github.com/tetris-swe-team10/tetris/issues/48)) | 전체 화면 디자인, P2P 게임 중 화면·복귀, 1·2차 재점검 | Figma 디자인과 화면 배치를 주로 맡았다. 디자인과 게임 중 화면 표시를 같은 사람이 맡아 화면 일관성을 지킨다. |

배분 원칙:

- **분량 균형:** 1·2차 병합 PR은 A 11개, B 9개, C 12개다(2026-11-18 조회). 3차도 각자 1주차 1개·2주차 1개 큰 작업을 병렬로 맡는다.
- **서로 기다리지 않게:** 공용 인터페이스(PR #56)를 먼저 병합하고, P2P는 「게임 시작」 버튼을 경계로 앞은 B, 뒤는 C가 맡는다.
- **공동 책임:** 테스트는 각자 만든 코드에 쓰고, 커버리지 70%는 B가 측정해 부족 영역을 공유한다. 리뷰는 다른 역할이 한다.

공통 계약은 [역할·인터페이스](../coordination/req3-roles.md), 운영은 [시작·종료 루틴](../session-routines.md)을 따른다.
자기 역할 문서 → 필요한 인터페이스 → 관련 코드·테스트 순서로 읽는다. 다른 역할의 전체 원문을 매번 읽지 않는다.

## 공통 완료 기준

- [ ] 요구사항 ID를 이슈에 연결하고 `feature/` 브랜치에서 작업
- [ ] 자기 변경과 직접 영향받는 테스트 실행, 명령·결과 기록
- [ ] PR에 동작·미완료·연동 조건 작성 → 다른 팀원 리뷰 → 답글·수정 → 병합
- [ ] 최종 통합 시 같은 커밋의 Line Coverage 70% 확인(B 측정, 공동 보강)
- [ ] 1·2차 요구사항과 비기능 요구사항도 최종 평가 대상인지 재점검
- [ ] 도전 과제는 별도 브랜치 유지, main 병합하지 않음

## 2026-11-16 사용자 승인 기본 규칙

자연 완성 2줄 이상만 공격한다. E/L/W로 지운 줄은 공격에서 제외하며, B/K는 자신에게 적용한다.
시간제한은 120초이고 동점은 무승부다. 두 사람에게 동일 난이도를 적용하며 같은 PC의 일시정지는 양쪽 모두 정지한다.
공격으로 상단이 넘치면 패배한다. 이는 교수님 원문에 직접 적힌 수치가 아닌 사용자 승인 팀 기본값이다.

## 상태 기록 방법

체크박스는 코드·검증 근거가 생겼을 때만 완료한다. 구현·검증·원격 병합을 구분한다.
상세 진행과 남은 문제는 각 역할 문서와 [인수인계](../coordination/status.md)에 기록한다.
