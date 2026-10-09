# tetris

Java21 / JavaFX21 / Gradle 기반 소프트웨어공학 팀 프로젝트.

```powershell
.\gradlew.bat :app:run
.\gradlew.bat :app:test
.\gradlew.bat :app:uiTest
```

`uiTest`는 실제 JavaFX 창을 사용하는 데스크톱 테스트이며 일반 로직 테스트와 분리되어 있습니다.

게임 시작 전에 난이도를 선택합니다. 한 판 동안 난이도를 변경할 수 없고, 기록은 모드·난이도별로 구분합니다.

점수·난이도 규칙, MVC 책임 분리, 삭제 상태와 팀원 연동 계약은 [구현 설명](docs/scoring-difficulty.md)을 참고하세요.
