# feature/colorblind-mode

생성자: 정수연
카테고리: 기능
상태: 진행 중

# 색맹 모드 구현 정리

> 담당: 정수연 · 브랜치: `feature/colorblind-mode`
> 

## 1. 개요

- 블록 7종이 모두 서로 다른 색으로 보이게 함
- 색맹 모드를 켜면 색맹 친화 팔레트와 블록별 패턴을 적용함 (적록색맹·청황색맹 모두 고려)
- 설정은 파일에 저장되고, 다음 실행 때 다시 불러옴
- 게임 도중 설정을 바꿔도 즉시 반영됨

## 2. 기존 문제

- `Board`가 쌓인 블록을 `int[][]`에 `1`로만 저장해서 블록 종류 정보가 사라짐
- 그래서 쌓인 블록은 전부 한 색(`#527A83`)으로만 표시됨
- `TetrominoType`에 색 정보가 없고, `GameView`는 색을 하드코딩함

## 3. 블록 디자인

| 블록 | 일반 모드 | 색맹 모드 | 패턴 |
| --- | --- | --- | --- |
| I | `#22C7E8` | `#56B4E9` | 가로선 |
| O | `#F4C430` | `#F0E442` | 점 |
| T | `#A45ADD` | `#CC79A7` | 격자 |
| S | `#00A69C` | `#009E73` | `/` 사선 |
| Z | `#E83E8C` | `#D55E00` | `\` 사선 |
| J | `#315BD6` | `#0072B2` | 세로선 |
| L | `#F08A24` | `#E69F00` | 교차 사선 |
- S/Z는 `/`와 `\`, J/L은 세로선과 교차선, J/T는 파랑·세로선과 분홍·격자로 구분
- 밝은 블록(I, O, T, L)에는 어두운 패턴, 어두운 블록(S, Z, J)에는 밝은 패턴을 씀
- 모든 블록에 어두운 외곽선을 넣음
- 패턴은 화면 기준으로 그려서 블록을 회전해도 방향이 바뀌지 않음
- 쌓인 블록, 떨어지는 블록, NEXT 미리보기에 모두 같은 스타일을 적용함
- 디자인은 팀원 시안을 따름 (유령 블록은 이번 범위에서 제외)

## 4. 구현 구조

| 파일 | 역할 | 변경 |
| --- | --- | --- |
| `game/Board.java` | 블록 종류 배열(`cellTypes`) 추가, `getCellType()` 제공 | 수정 |
| `ui/BlockPattern.java` | 패턴 종류 enum | 신규 |
| `ui/BlockStyle.java` | 채우기 색 + 패턴 + 패턴 색 | 신규 |
| `ui/BlockPalette.java` | 모드별(일반/색맹) 블록 스타일 표 | 신규 |
| `ui/GameView.java` | 팔레트로 블록 그리기, 설정 변경 시 즉시 다시 그림 | 수정 |
| `settings/SettingsManager.java` | 설정 파일 읽기/쓰기 | 신규 |
| `settings/GameSettings.java` | 현재 설정값을 메모리에서 하나로 관리 | 신규 |
| `App.java` | 시작 시 설정을 읽고 게임 화면에 전달 | 수정 |

### 설계 포인트

- **기존 동작 유지**: `Board`의 `cells`(0/1)는 그대로 두고 종류 배열만 따로 추가함. 기존 메서드 동작과 기존 테스트는 바꾸지 않음
- **로직과 화면 분리**: 색 정보는 게임 로직(`game`)이 아니라 화면(`ui`)의 `BlockPalette`에 둠. 모드 전환은 팔레트만 바꾸면 됨
- **설정값은 한 곳에서 관리**: `GameSettings`가 현재 값을 들고 있고, 값이 바뀌면 화면에 알리고 파일에 저장함 (JavaFX `BooleanProperty` 사용)

### 동작 흐름

1. 앱 시작 → `SettingsManager`가 설정 파일을 읽어 `GameSettings` 생성
2. 게임 화면 생성 → `GameSettings`의 값으로 팔레트를 정하고 변경 알림을 등록
3. 설정 변경 → `GameSettings` 값 변경 → 게임 화면이 즉시 다시 그림 + 파일 자동 저장
4. 다음 실행 → 저장된 값으로 시작
- 설정 파일 위치: `~/.team10-tetris/settings.properties` (Windows: `C:\Users\사용자명\.team10-tetris\`)
- 내용: `colorBlindMode=true` 또는 `false`
- 파일이 없거나 값이 잘못됐으면 일반 모드로 시작함
- 파일은 값을 저장할 때만 생기고, 읽기만 할 때는 만들지 않음

## 5. 테스트

| 테스트 | 확인 내용 |
| --- | --- |
| `BlockTypeTrackingTest` (7) | 블록이 굳을 때 종류 기록, 줄 삭제 시 종류도 함께 이동 |
| `BlockPaletteTest` (11) | 모드별 7색 모두 다름, 시안 색상값 일치, 패턴 구분, 패턴 명암 |
| `SettingsManagerTest` (8) | 저장/불러오기, 파일 없음·잘못된 값·읽기 실패 시 OFF, 다른 설정 유지 |
| `GameSettingsTest` (7) | 값 변경 시 자동 저장, 즉시 알림, 다음 실행 시 값 유지, 저장 실패해도 동작 |
- 기존 테스트는 수정하지 않고 새 테스트 파일만 추가함
- 기존 테스트 포함 전체 104개 통과

### 수동 확인 방법 (설정 화면이 생기기 전)

```powershell
# 색맹 모드로 실행
New-Item -ItemType Directory -Force "$env:USERPROFILE\.team10-tetris" | Out-Null
Set-Content "$env:USERPROFILE\.team10-tetris\settings.properties" "colorBlindMode=true"
.\gradlew.bat run

# 확인 후 원래대로
Remove-Item "$env:USERPROFILE\.team10-tetris\settings.properties"
```

---

## 6. 설정 화면 담당자에게 전달

### 색맹 모드 토글 연결 (이것만 하면 됨)

```java
CheckBox colorBlindToggle = new CheckBox("색맹 모드");
colorBlindToggle.selectedProperty()
        .bindBidirectional(gameSettings.colorBlindModeProperty());
```

- `gameSettings`는 `App.java`의 필드(`GameSettings`)
- 이 한 줄로 아래가 모두 자동 처리됨
    - 토글에 현재 상태 표시
    - 게임 화면 색 즉시 변경 (게임 도중 포함)
    - 설정 파일 저장
    - 다음 실행 때 불러오기
- 시작 화면 설정과 게임 중 설정 창 모두 같은 `gameSettings`를 쓰면 값이 자동으로 맞음

### 게임 중 설정 창 흐름 (기존 코드)

- 게임 화면의 설정 진입: `gameView.setOnSettingsRequested(...)`로 연결 (호출되면 게임이 일시정지됨)
- 설정 창을 닫을 때: `gameView.returnFromSettings()` 호출 (원래 일시정지 상태가 아니었으면 게임 재개)

### 주의 사항

- **설정 저장용 파일이나 클래스를 따로 만들지 말 것**: 두 군데에 저장되면 값이 어긋남
- 다른 설정(낙하 속도, 화면 모드 등)도 같은 구조로 추가하면 됨
    - `SettingsManager`에 `loadXxx()` / `saveXxx()` 추가 (저장 시 기존 항목은 유지되도록 되어 있음)
    - `GameSettings`에 `xxxProperty()` 추가 (색맹 모드와 같은 방식)
- `GameSettings`는 JavaFX 화면 코드와 분리되어 있어서 JUnit으로 바로 테스트 가능 (`GameSettingsTest` 참고)

---

## 7. 남은 작업

- [ ]  설정 화면에 색맹 모드 토글 연결