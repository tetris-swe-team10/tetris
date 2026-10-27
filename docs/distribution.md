# Windows 실행 패키지 빌드와 배포

대상은 Windows 11 x64다. JDK 21의 `jpackage`로 아이콘이 들어간 `Tetris.exe`, 의존 라이브러리와 전용 Java 런타임을 함께 묶는다. 설치 프로그램이 아닌 압축 해제형 배포이며 WiX나 관리자 권한을 요구하지 않는다.

## 개발자: 빌드와 패키징

저장소 루트에서 실행한다. 빌드에는 JDK 21을 권장하며 Gradle Wrapper가 프로젝트 도구 체인과 의존성을 준비한다. 첫 실행에는 인터넷이 필요할 수 있다.

```powershell
.\gradlew.bat :app:packageWindowsZip
```

별도 Gradle 설치는 필요 없다. 소스 컴파일, 리소스/JAR 생성, 의존성 수집, 실행 이미지 생성, ZIP 생성을 순서대로 실행한다. 테스트는 이 명령에 포함하지 않는다.

- 배포 ZIP: `app/build/distributions/Tetris-Windows-x64.zip`
- 실행 이미지: `app/build/native/windows/Tetris/`
- 실행 파일: `app/build/native/windows/Tetris/Tetris.exe`

Windows 패키지는 Windows x64에서 빌드해야 한다. 이 작업은 macOS 배포 패키지를 만들지 않는다.

## 사용자: 실행

1. ZIP을 원하는 폴더에 전부 압축 해제한다.
2. `Tetris` 폴더의 `Tetris.exe`를 실행한다.

`Tetris.exe` 옆의 `app`·`runtime` 폴더가 필요하므로 EXE만 따로 복사하지 않는다. Java·Gradle 설치와 실행 중 다운로드는 필요 없다. 설정과 점수 기록은 기존처럼 사용자 홈의 `.team10-tetris`에 저장한다.

## 자동 검증과 발표 증거

```powershell
.\gradlew.bat :app:verifyWindowsPackage
```

배포 ZIP 생성 후 별도 폴더로 압축을 풀어 다음을 검사한다.

- EXE, 실행 설정과 전용 JVM 파일 존재
- 압축 해제한 배포물의 파일 크기 합이 500 MB(500,000,000바이트) 이하인지 확인
- EXE에서 추출한 아이콘에 프로젝트 아이콘의 두 색상이 포함되는지 확인
- 실제 EXE를 실행해 `Tetris` 창 생성 확인 후 정상 종료

결과는 `app/build/reports/packaging/windows.json`에 저장한다. 데스크톱 Windows 세션이 필요하다. 이 검사는 게임 전체 기능, 최소 CPU/RAM에서의 실행이나 OS별 모든 렌더링을 검증하는 테스트가 아니다. 용량은 파일 크기 합이며 파일 시스템 할당 크기와는 다를 수 있다.

2026-10-27 Windows 검증에서 압축 해제 크기 약 97.3 MB(92.8 MiB), 아이콘 추출·전용 JVM 포함·새 폴더에서 EXE 실행과 정상 종료를 확인했다. 500 MB 이하 조건의 자동 검증 자료로 JSON 보고서를 제시할 수 있다. 파일 변경에 따라 용량은 달라지므로 제출할 ZIP을 대상으로 검증 명령을 다시 실행한다.

## 아이콘과 실행 진입점

- `app/src/main/resources/com/team10/tetris/icons/tetris.ico`: 16~256px 다중 크기 Windows EXE 아이콘
- 같은 폴더의 `tetris.png`: 게임 창 아이콘, `tetris.svg`: 벡터 원본
- 프로젝트를 위해 직접 구성한 블록 모양이다. `python tools/generate-icon.py`로 재생성할 수 있으며 일반 빌드에는 Python이 필요 없다.
- `Launcher.main()`이 `App.main()`을 호출한다. 비모듈 클래스패스 패키지에서 JavaFX Application을 직접 실행할 때 발생하는 특수 실행 경로를 피한다.

패키징 옵션 기준: [JDK 21 jpackage 문서](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jpackage.html).
