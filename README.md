# tetris

Java21 / JavaFX21 / Gradle 기반 소프트웨어공학 팀 프로젝트.

Windows 11 x64 배포 ZIP 생성: `./gradlew.bat :app:packageWindowsZip`

결과: `app/build/distributions/Tetris-Windows-x64.zip`. 전체를 압축 해제한 후 `Tetris/Tetris.exe`를 실행합니다.
아이콘과 전용 Java 21 런타임이 포함되므로 받는 사람은 Java를 설치하지 않아도 됩니다.
패키지 자동 검증: `./gradlew.bat :app:verifyWindowsPackage`. [배포 방법](docs/distribution.md)

```powershell
.\gradlew.bat :app:run
.\gradlew.bat :app:test
.\gradlew.bat :app:uiTest
```

`uiTest`는 실제 JavaFX 창을 사용하는 데스크톱 테스트이며 일반 로직 테스트와 분리되어 있습니다.

게임 시작 전에 난이도를 선택합니다. 한 판 동안 난이도를 변경할 수 없고, 기록은 모드·난이도별로 구분합니다.

점수·난이도 규칙, MVC 책임 분리, 삭제 상태와 팀원 연동 계약은 [구현 설명](docs/scoring-difficulty.md)을 참고하세요.

현재 블록의 착지 위치를 고스트 테두리로 표시하고, 좌우 끝에서 세로 점선으로 낙하 범위를 안내합니다.
이동·회전 시 갱신하며 B 암전 중에는 숨깁니다. [고스트 블록 명세](docs/ghost-block.md)

아이템 모드에서 10줄마다 새 NEXT에 작은 폭탄 `E`가 붙습니다.
고정 시 해당 칸 중심 3×3 안의 삭제 대상이 0.3초간 깜빡인 뒤 사라집니다.
폭발 자체로 점수·줄 수를 올리거나 위 블록을 떨어뜨리지 않습니다.
경계 조건과 추가 아이템 연동은 [작은 폭탄 명세](docs/small-bomb.md)를 참고하세요.
줄 삭제 `L`은 고정 시 L이 있는 줄을 꽉 차지 않아도 삭제하며, 점수·줄 수는 일반 줄 삭제와 같습니다. [줄 삭제 명세](docs/line-clear-item.md)
무게추 `W`는 너비 4칸 블록으로, 처음 닿으면 좌우가 잠기고 아래 4열을 한 칸씩 뚫으며 지운 뒤 바닥에서 사라집니다(파괴분 무득점). [무게추 명세](docs/weight-item.md)

전체 코드의 Line Coverage 보고서는 아래 명령으로 생성합니다. `uiTest`는 데스크톱이 필요합니다.

```powershell
.\gradlew.bat :app:test :app:uiTest :app:jacocoTestReport
```

보고서: `app/build/reports/jacoco/test/html/index.html`
