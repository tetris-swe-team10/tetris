# 프로젝트 문서 지도

> 정리일: 2026-11-09 · 작성 범위: 로컬 코드·기존 문서·첨부 Notion 사본 · 기능의 현재 실행 검증과 구분

## 먼저 읽을 문서

| 필요 | 문서 |
| --- | --- |
| AI와 작업 시작·이슈·브랜치·리뷰 운영 | [AI 작업 지침](ai-guide.md), 루트 [AGENTS.md](../AGENTS.md) |
| 전체 기능·코드 위치·개발 환경 | [프로젝트 개요](project-overview.md) |
| 평가 항목·테스트·커버리지 증빙 | [품질·평가 관리](quality-and-evaluation.md) |
| 기존 결정·해석·미결정 사항 | [결정 기록](coordination/decisions.md), [열린 질문](coordination/questions.md) |
| 팀원 기록·다음 작업·인수인계 | [작업 현황](coordination/status.md) |
| 노션 내용 요약·29개 본문·이미지·CSV | [노션 자료 정리](notion/README.md) |

## 기능별 상세 명세

| 기능 | 문서 |
| --- | --- |
| 점수·난이도·생성 확률·기록 형식 | [scoring-difficulty.md](scoring-difficulty.md) |
| 설정 저장·초기화·조작 키·기본 속도 | [settings.md](settings.md) |
| 고스트와 범위 안내선 | [ghost-block.md](ghost-block.md) |
| 작은 폭탄 E / 줄 삭제 L | [small-bomb.md](small-bomb.md), [line-clear-item.md](line-clear-item.md) |
| 암전 B / 회전 잠금 K / 무게추 W | [blackout.md](blackout.md), [rotation-lock.md](rotation-lock.md), [weight-item.md](weight-item.md) |
| 폰트·격자 / Windows 패키징 | [fonts.md](fonts.md), [패키징 PR #54의 배포 문서](https://github.com/tetris-swe-team10/tetris/blob/feature/windows-package/docs/distribution.md) (main 미병합) |

## 자료를 판단하는 기준

교수님 원문·e-class 답변과 팀 합의는 요구사항의 근거다. 코드는 구현 사실, 실행 보고서는 해당 커밋·환경에서의 검증 근거다. 어느 하나가 나머지를 자동으로 증명하지 않는다.

노션 원문은 당시 상태와 제안을 보존한다. 오래된 기능 문서에도 초기 통합 안내가 남아 있으므로 현재 기능은 [프로젝트 개요](project-overview.md)와 실제 코드를 대조한다. 숫자나 규칙이 충돌하면 [열린 질문](coordination/questions.md)에 남긴다.

참고한 운영 구조: [team19 AGENTS.md](https://github.com/2026ENDPoinT/team19/blob/main/AGENTS.md), [협업 방식](https://github.com/2026ENDPoinT/team19/blob/main/docs/08-collaboration.md). 결정·질문·현황을 분리하고 공통 AI 진입점을 두는 방식만 적용했다. 참고 저장소의 해커톤 제품·역할·일정·전용 승인 절차는 이 프로젝트의 규칙으로 가져오지 않았다.
