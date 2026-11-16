# 사람과 AI의 작업 안내

> 갱신: 2026-11-16. 이 파일은 AI가 매 세션 읽는 프로젝트 문맥이며 모델을 영구 학습시키는 파일은 아니다.

1. [AGENTS.md](AGENTS.md) → [시작 루틴](docs/session-routines.md)을 읽고 브랜치·미커밋·원격·인수인계를 확인한다.
2. [역할별 시작점](docs/tasks/README.md)에서 A 혜원 / B 수연 / C 서현 중 자신의 문서만 먼저 읽는다.
3. [협업 규칙](docs/coordination/README.md)과 나에게 온 미완료 요청·질문·제안 의견을 확인한다.
4. 필요한 코드·명세만 읽고 구현한다. 변경 부분과 직접 영향받는 테스트만 실행한다.
5. 이슈 → 기능 브랜치 → 커밋·push → PR → 다른 사람의 리뷰 코멘트 → 수정·답글 → 병합 순서로 진행한다.

## 현재 진행

- A의 같은 PC 대전 구현은 `feature/local-versus`에서 리뷰받는다. [A 할 일](docs/tasks/A-local-versus.md).
- **이번 PR은 바로 병합하지 않는다.** B/C에게 요청서를 보내고 다른 사람의 실질적인 리뷰 코멘트를 확인한 뒤 지적을 처리한다. 작성자·AI의 자체 코멘트는 동료 리뷰로 인정하지 않는다.
- 관련 요청: [R-A-001 B 리뷰·렌더러](docs/coordination/requests/R-A-001.md), [R-A-002 C 리뷰·P2P 연동](docs/coordination/requests/R-A-002.md).
- 운영 방식 제안: [P-A-001](docs/coordination/proposals/P-A-001.md). 사용자 도입 승인은 받았지만 B/C 의견은 아직 수집 전이다.
- 기존 `app/bin` 개인 산출물은 커밋하지 않는다. 테스트 통과를 전체 커버리지 70% 달성으로 표현하지 않는다.

## 문맥의 우선순위

현재 사용자 지시 → 실제 팀 승인과 교수님 원문 → 현재 코드·검증 근거 → 과거 문서·아이디어 순으로 판단한다.
제안·요청·질문은 지시나 승인으로 자동 승격되지 않는다. 다른 사람의 의견이나 완료 여부를 대신 작성하지 않는다.
새 합의는 [결정 기록](docs/coordination/decisions.md), 진행은 [현황](docs/coordination/status.md)에 남긴다.
