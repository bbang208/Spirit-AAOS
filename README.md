# Spirit

Pleos 차량 OS용 랩 타이머 & 레이싱 텔레메트리 앱입니다.

레이스 트랙에서 세션을 기록하고, GPS 게이트 로직으로 랩을 자동 감지하며, 섹터 스플릿과 실시간 타이밍 데이터를 표시합니다.

## Features

- **랩 타이밍** — GPS 기반 자동 랩/섹터 감지 및 타이밍
- **트랙 관리** — 프리셋 서킷 선택 또는 직접 GPS로 트랙 생성
- **실시간 텔레메트리** — 속도, G-Force, 트랙 맵 위 차량 위치 표시
- **세션 기록** — 세션별 랩 히스토리, 베스트 랩, 섹터 비교
- **고스트 런** — 이전 베스트 랩과 실시간 비교
- **차량 연동** — Pleos Vehicle SDK & Google Car API를 통한 차량 데이터 활용
- **주행 중 안전** — AAOS Driver Distraction Guideline 준수

## Tech Stack

| Category | Stack |
|---|---|
| Language | Kotlin (AGP 9.1 built-in) |
| Architecture | MVVM + Clean Architecture, Single Activity |
| UI | Android Views + DataBinding |
| DI | Hilt 2.59 |
| Network | Retrofit 3 + OkHttp 5 |
| Database | Room 2.7 |
| Navigation | Navigation Component 2.9 |
| Async | Kotlin Coroutines + StateFlow/LiveData |
| Platform | Pleos Vehicle SDK 2.0 + Android Car API |
| Min SDK | 34 |

## Build

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# Clean
./gradlew clean
```

## Project Structure

```
app/src/main/java/io/github/bbang208/spirit/
├── di/                  # Hilt DI modules
├── data/                # Data layer (API, Room, Vehicle repositories)
├── domain/              # Domain logic (timing, tracking, telemetry)
├── ui/                  # UI layer (Fragments, ViewModels, custom Views)
├── binding/             # DataBinding adapters
└── util/                # Utilities (GPS math, time formatting, etc.)
```

## License

All rights reserved.
