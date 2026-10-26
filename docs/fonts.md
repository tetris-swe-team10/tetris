# 공통 글꼴과 보드 격자

Mac과 Windows의 설치 글꼴 차이로 인한 화면 변화를 줄이기 위해 Noto Sans KR Regular·Bold 정적 OTF를 앱 리소스에 포함한다. 별도의 폰트 설치나 실행 중 다운로드는 필요 없다.

- 파일: `app/src/main/resources/com/team10/tetris/fonts/`
- 출처: notofonts/noto-cjk의 `Sans2.004` 릴리스, 한국어 지역별 서브셋. 원본 바이너리를 수정하지 않는다.
- 라이선스: SIL Open Font License 1.1. `OFL.txt`와 출처·파일 해시를 기록한 `README.md`를 함께 배포한다.
- `AppFonts.apply()`가 Regular·Bold를 등록하고 공통 `fonts.css`를 연결한다.
- 시작·모드 선택·설정·키 변경·게임·기록·게임 종료 화면에 적용한다. 인라인 글자 크기 스타일과 Canvas의 아이템 문자도 같은 폰트를 사용한다.
- 네이티브 창 제목 표시줄 등 OS가 그리는 영역은 적용 범위가 아니다. OS의 글자 렌더링과 모니터 색 표현까지 완전히 같게 만드는 변경은 아니다.

게임 보드 격자 색상을 `#1F2937`에서 `#344155`로 밝게 조정한다. 배경·블록 색상·게임 규칙은 유지한다.

검증 명령: `bash gradlew test uiTest --no-configuration-cache`.

`AppFontsViewTest`는 실제 JavaFX CSS가 Noto Sans KR Regular·Bold를 선택하는지 검증한다. 기존 반응형·설정·아이템·고스트 테스트도 함께 실행한다. Windows 실기 렌더링은 별도 확인이 필요하다.
