# 노션 자료 정리와 원문 목록

> 가져온 날짜: 2026-11-09 · 자료 유형: 제공된 Notion 내보내기 사본 · 현재 구현 현황과 구분

## 읽는 순서

- 현재 구조와 기능: [프로젝트 개요](../project-overview.md)
- 개발·이슈·PR 운영: [AI 작업 지침](../ai-guide.md)
- 요구사항과 검증 근거: [품질·평가 관리](../quality-and-evaluation.md)
- 기존 결정과 미결정 사항: [결정 기록](../coordination/decisions.md), [열린 질문](../coordination/questions.md)
- 역할·후속 작업: [인수인계 현황](../coordination/status.md)

## 출처와 보존 범위

첨부 `ExportBlock-aaf00280-a557-4650-bbeb-23a2c68bc045.zip`의 Markdown·CSV·이미지를 `archive/`에 원본 바이트 그대로 보존했다.
파일별 SHA-256과 원래 페이지 ID는 [출처 목록](source-manifest.json)에 있다. 이전에 제공된 CSV 전용 ZIP 대신 본문이 포함된 이번 ZIP을 사용했다.
제공된 사본을 정리한 것이며 실시간 Notion 전체를 동기화한 결과는 아니다. 공개 URL과 연결된 Notion에서는 해당 데이터베이스 본문을 읽지 못했다.

원문의 생성자는 작성자이지 새 작업의 자동 담당자가 아니다. `완료`, `리뷰 대기`, 체크박스, 커버리지 숫자는 원문 작성 당시의 기록이며 현재 main의 통과 증거로 재사용하지 않는다.
같은 제목이라도 작성자·페이지 ID가 다르면 별도 자료로 보존했다. 빈 `제목 없음` 템플릿도 삭제하지 않았다.

## 내용별 요약

| 묶음 | 정리한 핵심 내용 | 현재 적용 시 주의 |
| --- | --- | --- |
| 개발 운영·환경 | Java 21, JavaFX 21, Gradle Wrapper, JUnit, 기능별 브랜치와 `feat/fix/docs/test/refactor/chore` 커밋 | 개인 PC의 캐시 경로를 팀 공통 설정으로 강제하지 않음 |
| 1·2차 요구사항 | 화면별 기능, 난이도·확률, 아이템, 프로젝트 관리·커버리지 조건 | 노션 분석서는 교수님 원문 자체가 아님. 모호한 사항은 원문·e-class 답변과 대조 |
| Agile Planning | 사용자 스토리, 작업 배정, 완료 조건 | 수업 피드백에 따라 스토리 단위 책임을 먼저 나누고 실제 변경 이력과 연결 |
| 기능 구현 일지 | 시작 메뉴·게임·설정·색맹·점수·B/K/L의 구조와 연동 | 과거 미구현 표시를 오늘의 미구현으로 단정하지 않음 |
| 아이템 아이디어 | V/G/F, 성장 주사, 도박 상자, B/R/K 등 제안 | 현재 등록 E/B/K/L/W와 구분. 3% 별도 벌칙 추첨·B 3초·다음 블록 K는 과거 초안 |
| item 명세서 | E/B/K/L/W 공통 지급·발동·종료·점수 규칙 | 코드와 각 아이템 명세를 함께 확인 |
| 중간 점검·현재 이슈 | 키 변경·저장·초기화·배포·입력 테스트·리뷰 증빙의 당시 공백 | 해결 여부는 커밋/PR/새 실행 결과로 갱신 |
| 아키텍처 패턴 토론 | 법인차량 관리시스템 X의 패턴 적용 과제 | 테트리스 구현 아키텍처로 혼동하지 않음 |

## 원문 바로가기

| 문서 | 작성자 | 원문 상태 | 분류 |
| --- | --- | --- | --- |
| [1차 요구사항 분석 ⭐️](archive/1%EC%B0%A8%20%EC%9A%94%EA%B5%AC%EC%82%AC%ED%95%AD%20%EB%B6%84%EC%84%9D%20%E2%AD%90%EF%B8%8F%203ddd09ed490a8087b7eed60edfc5e33c.md) | 박서현 | 완료 | 분석 |
| [제목 없음](archive/%EC%A0%9C%EB%AA%A9%20%EC%97%86%EC%9D%8C%203ddd09ed490a80f3a2b2d9ea0d05f8b1.md) | 박서현 | 시작 전 | 미기재 |
| [개발 운영 방식](archive/%EA%B0%9C%EB%B0%9C%20%EC%9A%B4%EC%98%81%20%EB%B0%A9%EC%8B%9D%203dfd09ed490a8025aed5fadfc184ad35.md) | 박서현 | 진행 중 | 미기재 |
| [개발 환경 초기 설정](archive/%EA%B0%9C%EB%B0%9C%20%ED%99%98%EA%B2%BD%20%EC%B4%88%EA%B8%B0%20%EC%84%A4%EC%A0%95%203e1d09ed490a80489253db111eac35ff.md) | 정혜원 | 완료 | 미기재 |
| [feature/start-menu](archive/feature%20start-menu%203e2d09ed490a803a9747e710902295e9.md) | 박서현 | 완료 | 기능 |
| [2026-09-26 개발일지](archive/2026-09-26%20%EA%B0%9C%EB%B0%9C%EC%9D%BC%EC%A7%80%203e7d09ed490a80a0aae7f2637315d3fb.md) | 정수연 | 완료 | 로그 |
| [테트리스 게임 조작법](archive/%ED%85%8C%ED%8A%B8%EB%A6%AC%EC%8A%A4%20%EA%B2%8C%EC%9E%84%20%EC%A1%B0%EC%9E%91%EB%B2%95%203e9d09ed490a807dafbbebc486827a06.md) | 정혜원 | 완료 | 미기재 |
| [테트리스 개발 현황 및 다음 작업 공유 (2026.10.02 기준)](archive/%ED%85%8C%ED%8A%B8%EB%A6%AC%EC%8A%A4%20%EA%B0%9C%EB%B0%9C%20%ED%98%84%ED%99%A9%20%EB%B0%8F%20%EB%8B%A4%EC%9D%8C%20%EC%9E%91%EC%97%85%20%EA%B3%B5%EC%9C%A0%20%282026%2010%2002%20%EA%B8%B0%EC%A4%80%29%203edd09ed490a8079aedbfbedf16f651f.md) | 정혜원 | 진행 중 | 분석 |
| [Agile Planning (토론과제)](archive/Agile%20Planning%20%28%ED%86%A0%EB%A1%A0%EA%B3%BC%EC%A0%9C%29%203efd09ed490a8002b7e6c6fe5a8bbfff.md) | 박서현 | 완료 | 과제 |
| [feature/colorblind-mode](archive/feature%20colorblind-mode%203efd09ed490a80519a82e9eee34a8dc2.md) | 정수연 | 진행 중 | 기능 |
| [색맹 모드 디자인 설계](archive/%EC%83%89%EB%A7%B9%20%EB%AA%A8%EB%93%9C%20%EB%94%94%EC%9E%90%EC%9D%B8%20%EC%84%A4%EA%B3%84%203efd09ed490a808d9959e3197321d726.md) | 박서현 | 완료 | 분석 |
| [Agile Planning](archive/Agile%20Planning%203efd09ed490a80e7a877f9dba3c9e2a9.md) | 정수연 | 완료 | 과제 |
| [Agile Planning](archive/Agile%20Planning%203f0d09ed490a80f08708f7069ad7e702.md) | 정혜원 | 완료 | 과제 |
| [2차 계획](archive/2%EC%B0%A8%20%EA%B3%84%ED%9A%8D%203f2d09ed490a80f4a94ac1b7bdbea17a.md) | 박서현 | 진행 중 | 분석 |
| [2차 요구사항 분석 ⭐️](archive/2%EC%B0%A8%20%EC%9A%94%EA%B5%AC%EC%82%AC%ED%95%AD%20%EB%B6%84%EC%84%9D%20%E2%AD%90%EF%B8%8F%203f3d09ed490a804aa726ec98450dd361.md) | 박서현 | 진행 중 | 분석 |
| [Application of Architectural Patterns](archive/Application%20of%20Architectural%20Patterns%203f3d09ed490a8053a0e4c7cdf5e0e720.md) | 박서현 | 완료 | 과제 |
| [색맹 모드 main으로 merge](archive/%EC%83%89%EB%A7%B9%20%EB%AA%A8%EB%93%9C%20main%EC%9C%BC%EB%A1%9C%20merge%203f4d09ed490a80939606c61f4303407d.md) | 정수연 | 완료 | 로그 |
| [아이템 모드 아이디어](archive/%EC%95%84%EC%9D%B4%ED%85%9C%20%EB%AA%A8%EB%93%9C%20%EC%95%84%EC%9D%B4%EB%94%94%EC%96%B4%203f4d09ed490a80b4852ac8b388068740.md) | 정수연 | 완료 | 아이디어 |
| [feature/scoring-difficulty](archive/feature%20scoring-difficulty%203f4d09ed490a80cf950fcff36ffe3aeb.md) | 정혜원 | 완료 | 기능 |
| [아이템 모드 아이디어](archive/%EC%95%84%EC%9D%B4%ED%85%9C%20%EB%AA%A8%EB%93%9C%20%EC%95%84%EC%9D%B4%EB%94%94%EC%96%B4%203f5d09ed490a80d48920d3aa57ba4788.md) | 박서현 | 완료 | 아이디어 |
| [아이템 모드 아이디어](archive/%EC%95%84%EC%9D%B4%ED%85%9C%20%EB%AA%A8%EB%93%9C%20%EC%95%84%EC%9D%B4%EB%94%94%EC%96%B4%203f6d09ed490a8008aab4c89c91fe3bf2.md) | 정혜원 | 완료 | 아이디어 |
| [현재 이슈 + 구현할 것](archive/%ED%98%84%EC%9E%AC%20%EC%9D%B4%EC%8A%88%20%2B%20%EA%B5%AC%ED%98%84%ED%95%A0%20%EA%B2%83%203f6d09ed490a804ab69ce5559863d61d.md) | 박서현 | 진행 중 | 이슈 |
| [[10팀] Application of Architectural Patterns](archive/%5B10%ED%8C%80%5D%20Application%20of%20Architectural%20Patterns%203f6d09ed490a806296c3e6e8748d9495.md) | 정혜원 | 완료 | 과제 |
| [feature/setting](archive/feature%20setting%203f6d09ed490a80f89e1fe33b93cc1664.md) | 박서현 | 완료 | 기능 |
| [명세서 중간 점검](archive/%EB%AA%85%EC%84%B8%EC%84%9C%20%EC%A4%91%EA%B0%84%20%EC%A0%90%EA%B2%80%20405d09ed490a803298c1f0fecbba7123.md) | 정혜원 | 진행 중 | 분석 |
| [feature/itme_rotationLock](archive/feature%20itme_rotationLock%20405d09ed490a8090b830c29aa8a5314f.md) | 박서현 | 완료 | 기능 |
| [feature/line-clear-item](archive/feature%20line-clear-item%20405d09ed490a80d7b6b1d9346a5c1960.md) | 정수연 | 시작 전 | 미기재 |
| [feature/item_blackout](archive/feature%20item_blackout%20405d09ed490a80f39b83d480c6b743d5.md) | 박서현 | 완료 | 기능 |
| [item 명세서](archive/item%20%EB%AA%85%EC%84%B8%EC%84%9C%20406d09ed490a80d09f20d876d84eb3b5.md) | 박서현 | 시작 전 | 분석 |

원문은 증거 보존용이다. 새 합의나 현황 변경은 원문을 덮어쓰지 말고 요약 문서와 결정·질문 기록에 추가한다.
