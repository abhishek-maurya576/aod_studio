# AOD Studio

**AOD Studio** is a custom Always-On Display (AOD) design engine and visual editor built for Android (specifically optimized for **Vivo OriginOS 6** on Android 16). It features a real-time Canvas rendering pipeline, interactive drag-and-drop theme editor, built-in theme library, system hardware monitoring (battery, notifications, media session), and an AMOLED burn-in protection engine.

---

## Technical Features

### Real-Time Canvas Rendering Engine
- **Custom Android View Renderer (`AODRenderView`):** Executes high-performance 2D Canvas rendering decoupled from main UI loops.
- **Battery-Friendly 1Hz Frame Rate Capping:** Throttles redraw cycles to 1 Hz (1000ms minimum interval) to maximize AMOLED power efficiency.
- **12 Dynamic Element Sub-Renderers:**
  - **Digital Clock:** Configurable 12h/24h formats (`HH:mm`, `hh:mm a`) with custom typography weights (`THIN`, `MEDIUM`, `BOLD`).
  - **Analog Clock:** Face markers (ticks/dots), hour, minute, and second hands.
  - **Typography & Radial Orbit Clocks:** Vertical stacked digits and orbital ring dial tracks.
  - **Date Display:** Exception-safe date formatting (`EEE • MMM dd`, `EEEE, dd MMMM`).
  - **Battery Monitoring:** Zero-polling `BroadcastReceiver` tracking battery percentage, charging state, and plugged mode (AC/USB/Wireless).
  - **Privacy-First Notifications:** Listens to `NotificationListenerService` callbacks storing only minimal metadata (package name, timestamp, category). Message text is not stored.
  - **Media Session Playback:** Direct integration with Android `MediaController` / `MediaSessionManager` for track title, artist, album, and playing state.
  - **Shapes, Images & Group Containers:** Support for vector graphics, decorative frames, and nested element grouping.

---

### Interactive Visual Editor
- **Live Canvas Preview:** Instant real-time rendering of elements as properties change.
- **Property Inspector Panel:** Sliders for X/Y coordinates (0–1080px / 0–2400px), font size (12–120sp), hex color text input (`#RRGGBB`), and element deletion.
- **Snap-to-Center Alignment Guides:** Automated snapping to 540f horizontal / 1200f vertical canvas center within a 20px threshold.
- **Undo / Redo History Stack:** Full state snapshot history with `undo()` and `redo()` support.
- **Layering & Z-Index Reordering:** Move elements up and down in rendering order.

---

### AMOLED Burn-In Protection Engine
- **Periodic Pixel Shift (`BurnInManager`):** Calculates 5-minute periodic x/y pixel shifts along a smooth circular orbital trajectory bounded strictly within max `±4px` (`BURN_IN_MAX_OFFSET_PX`).
- **Subpixel Opacity Capping:** Automatically caps static element alpha to max `85%` (`0.85f`) to prevent static subpixel degradation.

---

### System Activation & Vivo OriginOS 6 Optimization
- **System Window Overlay (`AODWindowOverlayManager`):** Utilizes `TYPE_APPLICATION_OVERLAY` with lockscreen flags (`FLAG_SHOW_WHEN_LOCKED`, `FLAG_DISMISS_KEYGUARD`, `FLAG_KEEP_SCREEN_ON`).
- **Display Cutout Notch Support:** Configured with `LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES` for edge-to-edge pure `#000000` AMOLED black rendering on Vivo T4 Pro.
- **Foreground Service (`AODForegroundService`):** Declared with `foregroundServiceType="specialUse"` (Android 14+ / 16 compatible) with `ACTION_SCREEN_OFF` / `ACTION_SCREEN_ON` broadcast receivers for auto-display when device locks.

---

## Architecture & Technology Stack

| Layer | Technology |
| :--- | :--- |
| **Language** | Kotlin 2.3.21 |
| **Build System** | Android Gradle Plugin 9.3.1 |
| **UI Framework** | Jetpack Compose (Compose BOM 2026.06.00) |
| **Architecture Pattern** | Clean Architecture + MVVM |
| **Dependency Injection** | Hilt 2.60.1 |
| **Persistence** | Jetpack DataStore + Room 2.7.0 + Kotlinx Serialization |
| **Testing** | JUnit4 + MockK + Coroutines Test |

---

## Project Structure

```text
app/src/main/java/com/aodstudio/app/
├── aod/
│   ├── lifecycle/          # BurnInManager, BurnInOffset, BootReceiver
│   ├── overlay/            # AODWindowOverlayManager (TYPE_APPLICATION_OVERLAY)
│   ├── renderer/           # AODRenderer pipeline & sub-renderers (Clock, Date, Battery, etc.)
│   └── service/            # AODForegroundService (specialUse)
├── battery/                # BatteryRepository & Intent.ACTION_BATTERY_CHANGED receiver
├── config/                 # ThemeConfig tokens & AppConfig constants
├── domain/
│   ├── model/              # AODTheme, AODElement, AODCanvas, AODStyle, ThemeSerializer
│   ├── repository/         # ThemeRepository contract
│   └── usecase/            # GetThemes, SaveTheme, DeleteTheme, DuplicateTheme, ImportExportTheme
├── feature/
│   ├── editor/             # AODEditorScreen, AODEditorViewModel, PropertyPanel
│   ├── home/               # HomeScreen UI & AOD status cards
│   ├── library/            # ThemeLibraryScreen grid, ViewModel & category filter tabs
│   └── settings/           # SettingsScreen permission toggles & service switch
├── media/                  # MediaRepository & MediaController session listeners
├── navigation/             # Navigation Compose (AODNavHost, Routes)
└── notification/           # AODNotificationListenerService & NotificationRepository
```

---

## Getting Started

### Prerequisites
- JDK 17 (or Android Studio JBR)
- Android SDK 36 (Android 16 / OriginOS 6)
- Connected Android Device or Emulator (ADB)

### Building the Project

Assemble the debug APK:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleDebug
```

The compiled APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

### Running Unit Tests

Run the complete 56-test regression suite:

```powershell
.\gradlew.bat test
```

---

### Installing on Device via ADB

```powershell
adb install -r "app\build\outputs\apk\debug\app-debug.apk"
```

---

## License

Copyright (c) 2026 Abhishek Maurya. Released under the MIT License.
