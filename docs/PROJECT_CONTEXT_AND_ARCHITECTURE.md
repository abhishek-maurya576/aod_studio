# AOD Studio — Complete Project Vision, Architecture & AI Context Reference

---

## 1. Executive Summary & Vision

**AOD Studio** is a battery-efficient, fully customizable **Always-On Display (AOD)** engine and visual theme studio designed for Android devices with OLED/AMOLED displays (specifically optimized for Android 14–16, Vivo OriginOS, and AOSP-based systems).

### Core Vision:
- **True OLED Black:** Pure `#000000` background rendering ensures inactive screen pixels remain physically unpowered (0W draw).
- **Infinite Theme Customization:** A real-time WYSIWYG editor allows users to design, position, scale, colorize, and animate any combination of clock, battery, date, notification, music, biometric, and vector elements.
- **Flawless Off-Screen Lifecycle:** Wakes the physical screen onto a minimalist AOD layer on screen-off (`ACTION_SCREEN_OFF`), and dismisses cleanly with 0ms delay on unlock (`ACTION_USER_PRESENT`), double-tap, or fingerprint touch.
- **AMOLED Hardware Protection:** Integrated subpixel burn-in protection shifts coordinates in a smooth orbital trajectory every 5 minutes and caps maximum subpixel luminosity.

---

## 2. Technical Stack & Clean Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  - Jetpack Compose UI (Home, Library, Editor, Settings)     │
│  - Custom View Canvas Pipeline (AODRenderView, AODRenderer) │
│  - Lockscreen Host (AODActivity)                            │
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                      Domain Layer                           │
│  - Models: AODTheme, AODElement, AODElementType, AODStyle   │
│  - Templates: 9 Built-in Design Presets                     │
│  - Use Cases: GetThemesUseCase, SaveThemeUseCase, etc.      │
│  - Repositories Interfaces: Theme, Settings, Media, etc.    │
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                       Data Layer                            │
│  - Room / SharedPreferences / DataStore                     │
│  - Repositories Implementations                             │
│  - Notification Listener & MediaSession Services            │
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                    AOD System Engine                        │
│  - AODForegroundService (Screen state & WakeLock manager)   │
│  - BurnInManager (AMOLED orbital pixel shifting)            │
│  - PocketSensorManager (Proximity sensor suppression)       │
│  - ServiceWatchdog (AlarmManager survival & OEM recovery)   │
└─────────────────────────────────────────────────────────────┘
```

- **Language & Runtime:** Kotlin 2.0+ (JVM 17 / Android SDK min 26, target 36).
- **Dependency Injection:** Dagger Hilt (`@Singleton`, `@AndroidEntryPoint`).
- **State & Reactivity:** Kotlin Coroutines, `StateFlow`, `SharedFlow`.
- **Serialization:** `kotlinx.serialization` (JSON representation for all themes and element trees).

---

## 3. Theme Architecture & Data Schema

Themes in AOD Studio are completely decoupled data structures defined in domain models and serialized to JSON.

### 3.1 `AODTheme` Data Model
```kotlin
data class AODTheme(
    val id: String = generateId(),
    val name: String,
    val author: String = "AOD Studio",
    val version: Int = 1,
    val isPreset: Boolean = false,
    val isCustom: Boolean = true,
    val canvas: AODCanvas = AODCanvas(width = 1080, height = 2400, background = "#000000"),
    val elements: List<AODElement> = emptyList(),
    val metadata: Map<String, String> = emptyMap()
)
```

### 3.2 Built-in Preset Templates
Located under `com.aodstudio.app.domain.template.definitions`:
1. `MinimalAnalogTemplate` — Bauhaus-inspired minimalist analog clock with slim hour/minute hands and date badge.
2. `MinimalOrbitTemplate` — Orbital ring dials with battery percentage indicator.
3. `DigitalBoldTemplate` — Oversized typographic digital clock with vertical alignment.
4. `TypographyStackedTemplate` — Expressive stacked hour/minute typography with accent colors.
5. `OrbitRadialTemplate` — Multi-concentric ring clock with planetary orbital seconds.
6. `PixelFlexTemplate` — Retro dot-matrix clock styling.
7. `NeonPulseTemplate` — Cyberpunk glow clock with neon gradient outlines.
8. `RetroDigitalTemplate` — Vintage 7-segment digital LED clock.
9. `FadingDashTemplate` — Contemporary dashed track radial progress clock.

*Note: All preset templates are automatically seeded into local storage on initial app startup via `ThemeRepositoryImpl.initDefaults()`.*

---

## 4. Visual Elements & Rendering System

Every component visible on an AOD screen is an `AODElement` rendered via `AODRenderer`.

### 4.1 Supported `AODElementType`
| Element Type | Description | Key Properties / Render Attributes |
| :--- | :--- | :--- |
| `CLOCK` | Digital or analog clock representation | `clockStyle` (`DIGITAL`, `ANALOG`, `TYPOGRAPHY`, `RADIAL`, `ORBIT`, `STACKED`), `showSeconds` |
| `DATE` | Current date badge | `format` (e.g. `"EEE, MMM d"`, `"yyyy.MM.dd"`), `fontSize`, `color` |
| `BATTERY` | Live battery status & charging state | `batteryStyle` (`PERCENTAGE`, `ICON`, `RING`, `BAR`), charging animations |
| `NOTIFICATION` | App icons & count for unread notifications | Dynamic icon sizing, detailed mode text truncation, app icon bitmap rendering |
| `MUSIC` | Live media title, artist, album art & seekbar | Waveform progress bar, playback transport controls, auto-hides when no media |
| `FINGERPRINT` | In-display fingerprint vector guide | Vector ridges, guide ring, 0ms instant touch-to-unlock handoff to Keyguard |
| `SHAPE` | Vector shapes for backgrounds or accents | `shapeType` (`CIRCLE`, `RECTANGLE`, `ARC`), corner radius, stroke width, fill |
| `LINE` | Decorative dividers | Vector stroke coordinates, thickness, color |
| `RING` | Circular dials & borders | Radius, stroke thickness, fill toggle |
| `PROGRESS` | Linear or radial meters | Progress value, track color, bar color |
| `TEXT` | Custom static or dynamic text | Custom text string, font size, alignment, color |
| `IMAGE` | Custom bitmaps or vectors | Bitmap uri, scaling modes, tint |
| `GROUP` | Container for composite elements | Child element hierarchy, relative transforms |

### 4.2 Proportional Coordinate & Scaling Engine
All templates are authored in a virtual **`1080 x 2400` coordinate space**. When rendered inside `AODRenderView` or `AODPreviewScreen`:
1. Uniform scale factor is computed: `uniformScale = min(viewWidth / canvasW, viewHeight / canvasH)`
2. Letterbox offsets are applied: `offsetX = (viewWidth - contentW) / 2f`, `offsetY = (viewHeight - contentH) / 2f`
3. Result: Exact pixel-perfect alignment and zero distortion across all screen resolutions and aspect ratios.

---

## 5. Visual Theme Editor Architecture

The editor (`AODEditorScreen` + `AODEditorViewModel`) provides a real-time interactive canvas:

### 5.1 Editor Workflow & Capabilities
1. **Interactive Canvas View:**
   - Real-time rendering of the working theme.
   - Long-press / hold anywhere to trigger a full-screen ambient AOD simulation.
2. **Layer Selector Bar (`ElementSelectorRow`):**
   - Horizontal scrolling chip list showing all elements sorted by `zIndex`.
   - Visual element type icons with selection highlighting.
   - Quick **Add Element** dialog supporting all `AODElementType` values.
3. **Modular Property Inspector (`PropertyPanel`):**
   - **Common Controls:** X/Y Position sliders, Scale, Rotation, Opacity, Color Picker with centralized theme presets.
   - **Type-Specific Controls:** Clock style switchers, Date formats, Battery visual modes, Notification icon size sliders, Music widget controls.
4. **History & Undo/Redo:**
   - Stack-based state history tracking in `AODEditorViewModel`.
   - Unsaved change tracking (`isDirty`).
   - One-tap **Apply** action that saves to `ThemeRepository` and broadcasts `ACTION_RELOAD_THEME` to the live AOD service.

---

## 6. Off-Screen AOD Lifecycle & System Engineering

Third-party AOD implementation on Android requires handling complex OEM and system power constraints:

```
                  ┌───────────────────────────────┐
                  │    User Locks Phone / Sleep   │
                  └──────────────┬────────────────┘
                                 │
                     Intent.ACTION_SCREEN_OFF
                                 │
                                 ▼
                  ┌───────────────────────────────┐
                  │      AODForegroundService     │
                  │  - Acquire WakeLock           │
                  │  - Check Pocket Sensor        │
                  │  - Launch AODActivity         │
                  └──────────────┬────────────────┘
                                 │
                   BAL Allowed Intent (API 14-16)
                                 │
                                 ▼
                  ┌───────────────────────────────┐
                  │          AODActivity          │
                  │  - setShowWhenLocked(true)    │
                  │  - setTurnScreenOn(true)      │
                  │  - FLAG_KEEP_SCREEN_ON        │
                  │  - Low Brightness (0.01f)     │
                  │  - AMOLED Black Canvas Render │
                  └──────────────┬────────────────┘
                                 │
               User Unlocks / Double Tap / FOD Touch
                                 │
                                 ▼
                  ┌───────────────────────────────┐
                  │   0ms Instant Clean Dismiss   │
                  │     (finishAndRemoveTask)     │
                  └───────────────────────────────┘
```

### 6.1 Key Technical Solutions:
- **Physical Screen Wake over Keyguard:** `AODActivity` utilizes `setTurnScreenOn(true)` and `setShowWhenLocked(true)` with Background Activity Launch (BAL) exemption to wake the physical display into the AOD canvas.
- **Zero-Latency Dismissal:** Unlocking (`ACTION_USER_PRESENT`), double-tapping, or touching the in-display fingerprint sensor dismisses `AODActivity` via `finishAndRemoveTask()` with zero exit animation, smoothly handing off to the launcher.
- **AMOLED Burn-In Protection:** `BurnInManager` computes a ±4px orbital trajectory every 5 minutes (`BURN_IN_SHIFT_INTERVAL_MS = 300_000L`) to prevent static subpixel wear on OLED panels.
- **Pocket Detection:** `PocketSensorManager` turns off proximity sensor polling when the screen is active, and disables the AOD display completely when the device is inside a pocket or bag.
- **Process Survival (OriginOS / Vivo):** Uses `START_STICKY`, foreground notification channel, AlarmManager-based `ServiceWatchdog`, and custom OEM intent adapters (`VivoAdapter`) for high background power whitelist navigation.

---

## 7. Design Standards & Centralized Configuration Rules

### 7.1 The Central Configuration Rule
> **Never hardcode raw colors, typography sizes, spacing, or animation durations in components. All tokens must flow from centralized configuration files.**

- **`ThemeConfig.kt` (`app/src/main/java/com/aodstudio/app/config/ThemeConfig.kt`):**
  - Defines the single source of truth for colors, typography tokens, layout spacing, elevation, and AOD engine timing intervals.

### 7.2 Color & Aesthetic Rules:
- **Forbidden Colors:** Indigo, Blue, and generic blue variants (`#3B82F6`, `blue-500`, etc.) are strictly forbidden.
- **Preferred Palette:** Warm, premium neutral aesthetics (Amber `#E8A838`, Emerald `#5EC98A`, Rose `#E87C7C`, Slate `#1A1A1A`, Pure Black `#000000`).
- **Icons & Typography:** No standard emojis in production UI—use clean vector icons (`androidx.compose.material.icons`) and geometric Canvas paths.

---

## 8. Summary File Map for AI & Developers

| Component | File Path | Responsibility |
| :--- | :--- | :--- |
| **Theme Domain Model** | `app/src/main/java/com/aodstudio/app/domain/model/AODTheme.kt` | Root data schema for AOD themes |
| **Element Domain Model** | `app/src/main/java/com/aodstudio/app/domain/model/AODElement.kt` | Individual visual element schema & properties |
| **Element Types** | `app/src/main/java/com/aodstudio/app/domain/model/AODElementType.kt` | Enum of all supported element types |
| **Design Tokens** | `app/src/main/java/com/aodstudio/app/config/ThemeConfig.kt` | Centralized design tokens and AOD timing constants |
| **Core Renderer** | `app/src/main/java/com/aodstudio/app/aod/renderer/AODRenderer.kt` | Dispatches Canvas drawing to element sub-renderers |
| **Render View** | `app/src/main/java/com/aodstudio/app/aod/renderer/AODRenderView.kt` | Custom Android View hosting the 1Hz/60Hz render loop |
| **AOD Lockscreen Host** | `app/src/main/java/com/aodstudio/app/aod/ui/AODActivity.kt` | Full-screen lockscreen activity with screen-wake & FOD dismissal |
| **AOD Service** | `app/src/main/java/com/aodstudio/app/aod/service/AODForegroundService.kt` | Background service managing screen-off transitions |
| **Burn-In Manager** | `app/src/main/java/com/aodstudio/app/aod/lifecycle/BurnInManager.kt` | Calculates AMOLED orbital coordinate shifting |
| **Editor ViewModel** | `app/src/main/java/com/aodstudio/app/feature/editor/AODEditorViewModel.kt` | Manages editor state, history stack, and active theme |
| **Editor Screen** | `app/src/main/java/com/aodstudio/app/feature/editor/AODEditorScreen.kt` | WYSIWYG editor Compose UI |
| **Property Panel** | `app/src/main/java/com/aodstudio/app/feature/editor/components/PropertyPanel.kt` | Modular inspector for tweaking element properties |
