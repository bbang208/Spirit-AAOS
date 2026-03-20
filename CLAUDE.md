# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spirit is an Android application (package: `io.github.bbang208.spirit`) for the Pleos vehicle OS. It interfaces with the Pleos Vehicle SDK (`ai.pleos.playground:Vehicle`) to interact with vehicle systems (doors, display/temperature units, etc.). The app is a **lap timer / racing telemetry tool** that records sessions on race tracks, detects laps via GPS gate logic, tracks sector splits, and displays real-time timing data.

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew test                   # Run unit tests
./gradlew testDebugUnitTest --tests "io.github.bbang208.spirit.SomeTest"  # Single test
./gradlew connectedAndroidTest   # Instrumented tests
./gradlew clean                  # Clean build
```

## Architecture

- **Single-module** project (`:app`) using MVVM + Clean Architecture pattern
- **AGP 9.1.0** with built-in Kotlin (no separate kotlin-android plugin needed)
- **KSP** for annotation processing (not kapt)
- Android Views with **DataBinding** (not Compose)
- Min SDK 34, Target SDK 36
- Gradle version catalog (`gradle/libs.versions.toml`)
- Pleos Vehicle SDK from custom Maven (`nexus-playground.pleos.ai`)
- **Localization**: English (default) + Korean (`values-ko-rKR`)

### Package Structure (`io.github.bbang208.spirit`)

```
├── SpiritApplication.kt          # @HiltAndroidApp entry point
├── MainActivity.kt               # @AndroidEntryPoint, Vehicle SDK init
├── MainViewModel.kt              # @HiltViewModel
├── AppExecutors.kt               # Thread pool (diskIO, networkIO, mainThread)
├── di/                           # Hilt DI modules
│   ├── AppModule.kt              # SharedPreferences providers (encrypted)
│   ├── NetworkModule.kt          # OkHttpClient, Retrofit providers
│   ├── VehicleModule.kt          # Vehicle, Car, CarPropertyManager providers
│   ├── VehicleBindsModule.kt     # Vehicle repository bindings
│   ├── LocationModule.kt         # LocationProvider (real/mock GPS)
│   └── DatabaseModule.kt         # Room database & DAO providers
├── data/                         # Data layer
│   ├── ApiResponse.kt            # Sealed class (Success/Empty/Error)
│   ├── Resource.kt               # UI state wrapper (SUCCESS/ERROR/LOADING)
│   ├── Status.kt                 # Status enum
│   ├── NetworkBoundResource.kt   # Abstract network→UI pipeline
│   ├── models/                   # Domain models
│   │   ├── Session.kt            # Session with status (ACTIVE/COMPLETED/ABANDONED)
│   │   ├── Track.kt              # Track with outline, sectors, start line geometry
│   │   ├── Lap.kt                # Lap timing data
│   │   ├── LapSector.kt          # Sector-level timing within a lap
│   │   ├── Sector.kt             # Track sector definition
│   │   ├── GpsPoint.kt           # GPS coordinate data
│   │   ├── TelemetryPoint.kt     # Sensor telemetry data
│   │   └── GhostRun.kt           # Recorded ghost lap for comparison
│   └── source/
│       ├── remote/               # Retrofit API services
│       ├── local/
│       │   ├── prefs/            # EncryptedSharedPreferences
│       │   └── db/               # Room database
│       │       ├── SpiritDatabase.kt
│       │       ├── entity/       # SessionEntity, LapEntity, TrackEntity, etc.
│       │       ├── dao/          # SessionDao, LapDao, TrackDao, GhostDao
│       │       └── converter/    # Gson-based Room type converters
│       └── vehicle/              # Vehicle SDK & Car API repositories
│           ├── CarPropertyRepository[Impl].kt   # Google Car API wrapper
│           ├── VehicleSdkRepository[Impl].kt    # Pleos SDK wrapper
│           ├── DrivingStateRepository.kt        # isDriving state monitor
│           └── SpeedDataSource.kt               # Real-time speed feed
├── domain/                       # Domain logic layer
│   ├── timing/                   # Race timing engine
│   │   ├── LapTimer.kt           # @Singleton orchestrator (StateFlow outputs)
│   │   ├── LapDetector.kt        # GPS gate-based lap crossing detection
│   │   ├── SectorSplitter.kt     # Sector boundary crossing detection
│   │   ├── GForceCalculator.kt   # Lateral/longitudinal G-force calculation
│   │   ├── TimingModels.kt       # TimingState, LapCrossing, SectorState, etc.
│   │   └── GateUtils.kt          # Gate geometry utilities
│   ├── tracking/                 # GPS tracking & recording
│   │   ├── GpsTracker.kt         # @Singleton GPS buffer (StateFlow)
│   │   ├── LocationProvider.kt   # Location source interface
│   │   ├── MockLocationProvider.kt
│   │   └── TrackRecorder.kt      # Track outline recording & validation
│   ├── ghost/                    # (placeholder) Ghost comparison/playback
│   └── telemetry/                # (placeholder) Sensor data processing
├── ui/                           # UI layer
│   ├── common/                   # Reusable UI components
│   │   ├── DataBoundListAdapter.kt
│   │   ├── DataBoundViewHolder.kt
│   │   ├── LapListAdapter.kt
│   │   └── DistractionAwareFragment.kt  # AAOS driving overlay base class
│   ├── home/                     # Home screen (recent sessions)
│   ├── trackselect/              # Track selection (preset/my/nearby tabs)
│   ├── trackcreation/            # GPS-based track recording
│   ├── presession/               # Pre-session setup
│   ├── livetiming/               # Real-time lap timer display
│   ├── summary/                  # Post-session results
│   ├── detail/                   # Session detail deep-dive
│   ├── settings/                 # App preferences
│   └── widget/                   # Custom Views
│       ├── TrackMapView.kt       # Track outline + car position + ghost
│       ├── SectorBarView.kt      # Sector status indicator bar
│       └── GForceView.kt         # G-force gauge visualization
├── binding/                      # @BindingAdapter utilities
│   ├── RecyclerViewBinding.kt
│   └── TimingBindingAdapters.kt
└── util/                         # Utilities
    ├── Event.kt                  # Single-event LiveData wrapper
    ├── EventObserver.kt
    ├── Constants.kt              # GPS_SAMPLE_RATE_HZ=10, GATE_WIDTH=30m, etc.
    ├── GeoUtils.kt               # GPS math (distance, bearing, crossing)
    ├── TimeFormatter.kt          # ms → "MM:SS.mmm" format
    ├── LiveDataCallAdapter.kt    # Retrofit→LiveData adapter
    ├── LiveDataCallAdapterFactory.kt
    ├── CustomDebugTree.kt        # Timber tree with file:line tags
    └── AbsentLiveData.kt         # LiveData that never emits
```

### Data Flow

```
API Service (Retrofit, LiveData<ApiResponse>)
  → Repository (NetworkBoundResource)
    → ViewModel (LiveData<Resource<T>>)
      → Activity/Fragment (observes via DataBinding)

Domain Layer (LapTimer, GpsTracker — StateFlow outputs)
  → ViewModel (collects StateFlow)
    → Fragment (observes via DataBinding / LiveData bridge)

Room Database (DAO → Entity)
  → Repository
    → ViewModel
```

### Key Libraries

| Category | Library | Version |
|---|---|---|
| DI | Hilt | 2.59.2 |
| Network | Retrofit 3 + OkHttp 5 | 3.0.0 / 5.3.2 |
| Lifecycle | ViewModel, LiveData | 2.10.0 |
| Database | Room | 2.7.1 |
| Navigation | Navigation Component | 2.9.0 |
| Layout | FlexboxLayout | 3.0.0 |
| Coroutines | kotlinx-coroutines | 1.10.2 |
| Logging | Timber + Logger | 5.0.1 / 2.2.0 |
| Security | EncryptedSharedPreferences | 1.0.0 |
| Serialization | Gson | 2.12.1 |

### Design System

Pleos OS 디자인 시스템 (Material3 Light theme, Figma 기반):
- Theme: `Theme.Spirit` (Material3.Light.NoActionBar)
- Colors: `values/colors.xml` — Figma 변수 기반 (basic_00~900, text/icon/alpha 계층, switch, call 등)
- Typography: `values/type.xml` — `TextAppearance.Pleos.*` (Headline/Title/Body/Label + Feature Font)
- Styles: `values/styles.xml` — Widget.Pleos.* (Button, ToggleButton.Pill, CardView, Switch, Slider, TabLayout, etc.)
- Primary accent: `#02C265` (switch_on green)
- Background: `#F7F8FA` (basic_50), Surface: `#FFFFFF` (basic_00)
- Night theme mirrors Light (차량 OS는 항상 밝은 UI)

### Navigation

Single-Activity architecture with Navigation Component (`nav_graph.xml`):
```
HomeFragment → TrackSelectFragment → PreSessionFragment → LiveTimingFragment → SessionSummaryFragment
                    ↓                                                                    ↓
              TrackCreationFragment                                              HomeFragment (popUpTo)
HomeFragment → SessionDetailFragment
HomeFragment → SettingsFragment
```
All navigation actions have `nav_default_*_anim` enter/exit/popEnter/popExit animations.

### UI Notes

- **Pill Tab Toggle**: Track selection tabs use `LinearLayout` + individual `MaterialButton` (not `MaterialButtonToggleGroup`) to preserve pill shape with spacing. Selection logic is managed in Fragment code via `isChecked` state.
- **MaterialButtonToggleGroup `spacing` attribute is NOT available** in Material 1.13.0. Use `LinearLayout` with margins instead when individual button shapes must be preserved.

## Pleos UI Component Guide

Pleos Connect 디자인 시스템의 공식 컴포넌트 가이드. UI 구현 시 아래 구조와 규칙을 따를 것.
참고: https://document.pleos.ai/docs/connect/guide/docs-design/components/

### Actions

**Basic Button**
- 구조: Root(컨테이너) → Label + Prefix Icon + Suffix Icon
- 상태: Pressed(터치 중), Disabled(제어 불가), Loading(로더 표시+제어 불가)

**Toggle Button**
- 구조: Root → Label + Prefix/Suffix Icon
- 상태: Toggled(on/off), Pressed, Disabled
- 속성: Floating(shadow/색상 표현), Size(Large/Medium/Small)

**Text Button**
- 구조: Root → Label + Prefix/Suffix Icon
- 상태: Pressed, Disabled, Loading
- 속성: Size(Medium/Small)
- 용도: 부가 정보 링크, 아이콘으로 정보 전달력 강화

**Icon Button**
- 구조: Root → Icon
- 상태: Pressed, Disabled
- 속성: Style(Basic/Plain/Circle), Size(Large/Medium/Small)
- 직관적으로 인지 가능한 경우에만 사용

**Keypad**
- 구조: Root → Label + Prefix/Suffix Icon
- 상태: Pressed, Drag, Disabled
- 타입: Primary, Secondary, Special
- 입력 방식: Text, Voice
- 레이아웃: Numeric / QWERTY / Full keyboard
- 라운드: 25px

### Bar
- 용도: 앱 윈도우 사이즈 조정, 콘텐츠 스크롤
- 타입: Window Bar(사이즈 조정), Scroll Bar(콘텐츠 탐색)
- 구조: Root → Control → Handle (최소 drag 영역 확보)
- 상태: Variant(Light/Dark), Dragging, Disabled(handle 숨김), Pressed

### Controllers
- 용도: 볼륨, 온도 등 범위 내 값 조작
- 타입: Continuous Slider, Centered Slider, Stepper
- 구조: Root → Control → Handle + Track + Stepper + Prefix/Suffix Icon
- 속성: Disabled(min/max 도달 시), MinValue, MaxValue, Step
- 표현: Basic, Floating (밝은 배경용 구분)
- 볼륨 표시: 4단계 (max 100%, high 76-100%, mid 26-75%, low 1-25%)

### Indicator
- 용도: 캐러셀/단계 탐색 시 현재 위치 표시
- 구조: Root → Present Value + Next/Prev Value
- 속성: Size(Wide/Narrow)

### Dropdown
- 용도: 메뉴 이동, 속성 변경
- 구조: Root → Label + Prefix + Suffix + Container
- 상태: Opened, Pressed, Disabled
- 속성: Prefix(App Icon/Icon), Suffix(Switch/Icon), Size(Medium/Small)

### Page Navigation
- 용도: 앱 주요 네비게이션 구조
- 구조: Root → App Icon + Primary Menu + Secondary Menu + Suffix
- 타입: Main(기본), Depth(Back 버튼 표시)
- 속성: Primary(App Icon/Suffix), Secondary(Boolean), Subtitle(Boolean)

### Fields

**Text Field**
- 용도: 텍스트/패스워드/이메일 등 입력
- 구조: Root → Field + Cursor + Placeholder + Suffix
- 상태: Value(String), Focused, Disabled, ReadOnly, Invalid
- 속성: Type(Text/Tel/Url/Email/Password), Size(Medium/Small)

### Selections

**Checkbox** — 복수 선택용, 구조: Root → Control + Icon, 상태: Checked/Pressed/Disabled

**Radio** — 단일 선택용, 구조: Root → Control, 상태: Selected(Num/Null)/Pressed/Disabled/Invalid

**Switch**
- 용도: On/Off 상태 변경
- 구조: Root → Handle(제어 시각요소) + Track(상태 표시 시각요소)
- 상태: Checked(On/Off), Pressed, Disabled, Invalid

### Spinner
- 용도: 날짜/시간 선택
- 구조: Root → Selection Value + Contents
- 타입: Date, Time
- 드래그로 옵션 선택

### Segmented Menu (Tabs)
- 용도: 2개 이상의 콘텐츠/기능 옵션 전환
- 구조: Root → Tab Container → Tab Item(Label + Selected Indicator)
- 상태: Enable(기본 터치 가능), Pressed, Selected
- 타입: Box(채움), Line(밑줄)
- 속성: Fitted — True(전체 너비 균등 분할) / False(좌측 정렬, 자연 간격)

### Popups

**System Popup**
- 용도: 앱 내 기본 팝업, Body 영역에 커스텀 콘텐츠 가능
- 구조: Root → Title + Description(선택) + Body(선택) + Confirm Action + Cancel Action(선택)
- 속성: Header(Icon/SubTitle/Description), Body(Boolean), Size(Large/Medium), Button Align(Single/Vertical/Horizontal)

**Toast Popup**
- 용도: 앱 운영 중 임시 시스템 알림
- 구조: Root → Toast Message
- 패딩: 24dp 간격
- 최대 너비로 제공

### Widgets
- 구조: Root → Handle(접기용 드래그) + Pagination(총 개수/현재 위치) + Title/Action + Information

## Driver Distraction Guideline (주행 중 UI 제약)

차량용 앱은 AAOS 운전자 주의 분산 가이드라인을 준수해야 한다. UI 설계 시 반드시 아래 규칙을 따를 것.

### 조작 제한
- 원하는 기능에 **2~3번의 터치 이내**로 도달할 수 있도록 메뉴 구조를 단순화
- 자주 쓰는 기능은 첫 화면·상단에 배치, 드롭다운·다단 메뉴 남용 금지

### 터치 영역 수치 기준
- 버튼 최소 크기: **125×125 px 이상**
- 버튼 간 최소 간격: **4 px 이상**
- 큰 터치 영역(large touch target) + 높은 대비(high-contrast) 필수

### 텍스트·콘텐츠 제한
- 텍스트는 최소화, 긴 텍스트 스크롤 금지
- 주행 중 이미지 노출 최소화 (허용: 앨범 아트 1개, 아이콘, 내비 안내 이미지)
- **주행 중 동영상·애니메이션·움직이는 이미지 출력 금지**
- UI 요소의 자동 움직임 금지 (사용자 조작에 의해서만 동작)

### 주행 중 금지 패턴
- 게임, 인터넷 서핑, 피트니스 등 주의 산만 활동
- 영상 재생
- 복잡한 폼 입력
- 키보드 입력 → 프리셋 선택, 최근 항목, 음성 입력 우선

### 주행 vs 정차 분리
- 게임·엔터테인먼트·세부 설정 메뉴는 **정차·주차 시에만** 접근 가능
- 앱은 주행 중 제한 상태를 감지하여 **UI/기능을 자동으로 축소·비활성화**하는 로직을 구현해야 함
- 운전 관련 정보(내비 경로 안내)에 우선순위, 운전 중요 오디오(내비 음성 안내) 최우선

### 참고 문서
- Pleos Driver Distraction Guideline: https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/driver-distraction-guideline

## Key Integration: Pleos Vehicle SDK

`Vehicle(context)` → `vehicle.initialize()` to start, `vehicle.release()` to tear down. Subsystems via getters (`getDoor()`, `getDisplay()`). Callbacks use `onComplete`/`onFailed` pairs. Permissions: `pleos.car.permission.*` in manifest.

### Two API Layers

1. **Google Car API (VHAL)**: `android.car.Car`, `CarPropertyManager`, `VehiclePropertyIds` — 표준 AOSP 차량 속성 (속도, 기어 등)
   - `useLibrary("android.car")` 필수 (`build.gradle.kts` `android {}` 블록)
   - 속도 단위: VHAL은 m/s 반환 → km/h 변환 시 `* 3.6f`
   - `CAR_SPEED`는 dangerous permission → 런타임 요청 필요
2. **Pleos Vehicle SDK**: `ai.pleos.playground.vehicle.Vehicle` — Pleos 전용 API (조향각, 도어 등)

### Vehicle Lifecycle

- `vehicle.initialize()` in `onCreate()`
- `vehicle.release()` in `onDestroy()`
- 콜백 등록/해제: `onStart()`/`onStop()` 패턴 권장

### AAOS Distraction Optimization

- MainActivity에 `<meta-data android:name="distractionOptimized" android:value="true" />` 필수
- 이 메타데이터가 없으면 주행 중 앱이 차단됨
- `DistractionAwareFragment` 기반 클래스가 `isDriving` 상태를 모니터링하여 driving overlay 표시

### DI Structure

- `VehicleModule`: Vehicle, Car, CarPropertyManager `@Provides`
- `VehicleBindsModule`: Repository `@Binds`
- `LocationModule`: LocationProvider `@Provides` (real/mock)
- `DatabaseModule`: SpiritDatabase, SessionDao, LapDao, TrackDao, GhostDao `@Provides`
- `CarPropertyRepository`: Google Car API 래핑 (속도, 기어 등)
- `VehicleSdkRepository`: Pleos SDK 래핑 (도어, 디스플레이 등)
- `DrivingStateRepository`: 주행 상태 모니터링
