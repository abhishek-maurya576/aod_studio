# AOD Studio — Lessons Learned & Work Done Master Log

This document serves as an exhaustive knowledge dump of all architectural changes, feature implementations, layout refactorings, hardware integrations, and technical lessons learned during the development of **AOD Studio**.

---

## 🎯 1. Work Accomplished & Features Built

### 🎨 A. Template Presets & Fancy Typography Engine
- **New Built-in Templates (`assets/themes/`):**
  - `retro_digital.aod.json`: Retro Cyber Digital Clock with Amber accents and `CYBER` monospace typography.
  - `pixel_flex.aod.json`: Pixel Flex layout with Emerald `NEON` typography and percentage battery bar.
  - `fading_dash.aod.json`: Fading Dash layout with Rose `CURSIVE` typography and fade animations.
  - `neon_pulse.aod.json`: Pulsing Cyber layout with Cyan/Purple accents and `PULSE` animation engine.
- **Fancy Typography Engine ([RendererUtils.kt](file:///c:/my_data/projects/Android_/AOD_Studio/app/src/main/java/com/aodstudio/app/aod/renderer/RendererUtils.kt)):**
  - Added support for 7 custom typeface families: `DEFAULT`, `MONO`, `SERIF`, `DISPLAY`, `CYBER`, `CURSIVE`, `NEON`.

### 🎛️ B. Custom Theme Editor & Layout Refactoring ([AODEditorScreen.kt](file:///c:/my_data/projects/Android_/AOD_Studio/app/src/main/java/com/aodstudio/app/feature/editor/AODEditorScreen.kt))
- **45%-55% Proportional Layout Split:**
  - **Top Section (45% Height):** Centered, un-squished `AODRenderView` canvas preview.
  - **Bottom Section (55% Height):** Property panel constrained with sticky header and scrollable body. Added `48.dp` bottom padding to clear system gesture navigation bars.
- **Press & Hold Long-Press Full-Screen Preview:**
  - Pressing and holding a finger on the top preview canvas triggers an instant **100% full-screen AMOLED preview** (`#000000` background) without top bars or UI chrome. Releasing the finger immediately dismisses the preview.
- **Element Layer Selector Bar ([PropertyPanel.kt](file:///c:/my_data/projects/Android_/AOD_Studio/app/src/main/java/com/aodstudio/app/feature/editor/components/PropertyPanel.kt)):**
  - Horizontal chip row displaying all elements currently in theme (`[ ⏰ Clock ]`, `[ 📅 Date ]`, `[ 🔋 Battery ]`, `[ 💬 Text ]`, `[ 🔔 Notification ]`, `[ 🎵 Music ]`, `[ ➕ Add New ]`).
- **Position Presets:**
  - Quick chips for `TOP` (Y=400), `CENTER` (X=540, Y=1200), `BOTTOM` (Y=1800), `LEFT` (X=250), `RIGHT` (X=830), and text alignment (`LEFT`, `CENTER`, `RIGHT`).

### 🎵 C. Android 16–17 Material You Squiggly Wave Music Player ([MusicElementRenderer.kt](file:///c:/my_data/projects/Android_/AOD_Studio/app/src/main/java/com/aodstudio/app/aod/renderer/renderers/MusicElementRenderer.kt))
- **Dynamic Sine-Wave Path Animation:**
  - Renders a continuous squiggly progress bar path using `android.graphics.Path` with `sin(x * frequency + phase)` animated continuously via `System.currentTimeMillis()`.
- **5 Customizations in PropertyPanel:**
  1. `Music Style`: `WAVY_PROGRESS` (Android 16 Wave), `CLASSIC` (Card), `COMPACT` (Single Line), `MINIMAL` (Icon Only), `NEON_WAVE` (Glow Wave).
  2. `Wave Intensity`: `LOW`, `MEDIUM`, `HIGH`.
  3. `Show Album Thumbnail`: Toggle (true/false).
  4. `Show Media Control Buttons (⏮ ▶ ⏭)`: Toggle (true/false).
  5. `Position Presets`: `BOTTOM` (Y=1800), `CENTER` (Y=1400), `TOP` (Y=500).

### ⚙️ D. Hardware Integrations & System Service Logic
- **Pocket Detection (`PocketSensorManager.kt`):**
  - Uses Android `SensorManager` proximity sensor (`Sensor.TYPE_PROXIMITY`) to automatically hide the overlay when placed in a pocket.
- **Double-Tap to Exit:**
  - Attached gesture listener in `AODWindowOverlayManager.kt` allowing users to double-tap anywhere on the AOD screen to exit the overlay.
- **PowerManager WakeLock & Screen-Off Flags ([AODForegroundService.kt](file:///c:/my_data/projects/Android_/AOD_Studio/app/src/main/java/com/aodstudio/app/aod/service/AODForegroundService.kt)):**
  - `ACTION_SCREEN_OFF`: Acquires `PowerManager.PARTIAL_WAKE_LOCK` and attaches `TYPE_APPLICATION_OVERLAY` with `FLAG_TURN_SCREEN_ON` + `FLAG_KEEP_SCREEN_ON` at `0.01f` brightness.
  - `ACTION_SCREEN_ON` / `ACTION_USER_PRESENT`: Releases WakeLock and immediately hides overlay so it doesn't linger after unlock.
- **"Apply to AOD Screen" Handler ([AODPreviewScreen.kt](file:///c:/my_data/projects/Android_/AOD_Studio/app/src/main/java/com/aodstudio/app/feature/preview/AODPreviewScreen.kt)):**
  - Floating action button in full-screen preview that saves theme, sets active ID in `ThemeRepository`, and starts `AODForegroundService`.

---

## 🛠️ 2. Issues Fixed & Resolved

1. **Aspect Ratio & Sub-Renderer Scaling Fix:**
   - **Problem:** Sub-renderers ignored container offset and calculated raw pixel dimensions.
   - **Fix:** Implemented uniform `scaleFactor = min(scaleX, scaleY)` and centered letterbox offsets `contentOffsetX` & `contentOffsetY` with `RendererUtils.getDrawX()` and `getDrawY()`.
2. **Notification Icon Visibility Fix:**
   - **Problem:** When notification count was 0 in editor/preview mode, `NotificationElementRenderer` returned early, rendering nothing.
   - **Fix:** Added preview fallback mode (`"🔔 2"` or `"🔔  💬  ✉️"`) so notification elements are always visible and customizable.
3. **Duplicate Element Naming Fix:**
   - **Problem:** Adding elements via `+` button produced identical chip titles (e.g. two "Battery" chips).
   - **Fix:** Updated `AODEditorViewModel.kt` to append indexed names (e.g. `Battery 1`, `Battery 2`, `Notification 1`, `Notification 2`).
4. **Editor Screen Layout Squishing Fix:**
   - **Problem:** PropertyPanel lacked height constraints and expanded to ~80% height, squishing canvas preview to ~20%.
   - **Fix:** Assigned `weight(0.45f)` to Preview Canvas and `weight(0.55f)` to PropertyPanel with internal scrollable column.
5. **Battery Optimization Permission Grant:**
   - Whitelisted `com.aodstudio.app.debug` on system deviceidle whitelist via ADB (`dumpsys deviceidle whitelist +com.aodstudio.app.debug`).

---

## 💡 3. Key Lessons Learned for Future AI Agents

1. **AGP 9.0+ Plugin Rules:**
   - Do NOT apply `org.jetbrains.kotlin.android` plugin in `build.gradle.kts` (AGP 9.0+ handles Kotlin compilation natively).
   - Do NOT use `kotlinOptions { jvmTarget = "17" }` inside `android {}` block (managed via `compileOptions`).

2. **Android 14+ / 16 Service & Receiver Rules:**
   - Dynamic `BroadcastReceiver` registrations must specify `Context.RECEIVER_NOT_EXPORTED`.
   - `startForeground()` requires explicit `ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE` parameter.

3. **WindowManager Screen-Off AOD Rendering Rules:**
   - Third-party `TYPE_APPLICATION_OVERLAY` windows require `FLAG_TURN_SCREEN_ON` + `FLAG_KEEP_SCREEN_ON` at `0.01f` brightness + `PowerManager.PARTIAL_WAKE_LOCK` to keep the display panel alive when `ACTION_SCREEN_OFF` fires.
   - On custom OEM ROMs (OriginOS / Vivo), the user must disable default system AOD in **Settings → Display & Brightness → Always On Display** to prevent system AOD overlap.

4. **Aspect Ratio Canvas Transformation Math:**
   - Never use separate X and Y scale factors for canvas element rendering.
   - Use `scaleFactor = min(viewW / canvasW, viewH / canvasH)` for BOTH coordinates and font sizes, centered via `contentOffsetX = (viewW - canvasW * scaleFactor) / 2f` and `contentOffsetY = (viewH - canvasH * scaleFactor) / 2f`.
