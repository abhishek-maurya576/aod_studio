# AOD Studio: Off-Screen Display Architecture & Bug Analysis Report

---

## 1. Executive Summary & Complete Execution Flow

This report provides an in-depth architectural and logical analysis of the complete flow for applying, enabling, and rendering the Always-On Display (AOD) on the off-screen display in AOD Studio.

### Complete Flow Trace:
1. **Apply & Enable Phase:**
   - The user triggers an apply action (`AODPreviewViewModel.applyThemeToAod()`, `ThemeLibraryViewModel.activateTheme()`, or `AODEditorViewModel.saveTheme()`).
   - The theme is persisted via `SaveThemeUseCase.execute(theme)` to internal JSON storage (`ThemeStorage`).
   - The active theme ID is persisted to DataStore via `SaveThemeUseCase.setActive(theme.id)`.
   - The UI checks `Settings.canDrawOverlays(context)` and attempts to start `AODForegroundService.startService(context)`.

2. **Service Lifecycle & Registration Phase:**
   - `AODForegroundService.onCreate()` starts a low-importance ongoing foreground service notification (`SPECIAL_USE` on Android 14+), registers a dynamic `BroadcastReceiver` for `ACTION_SCREEN_OFF`, `ACTION_SCREEN_ON`, and `ACTION_USER_PRESENT`, and initiates pocket detection via `PocketSensorManager`.
   - `AODForegroundService.onStartCommand()` is invoked.

3. **Screen-Off & Display Hijack Phase:**
   - When the user presses the power button or the screen times out, the system broadcasts `Intent.ACTION_SCREEN_OFF`.
   - `AODForegroundService.onScreenOff()` receives the broadcast.
   - It attempts to acquire a `PowerManager.FULL_WAKE_LOCK` with `ACQUIRE_CAUSES_WAKEUP`.
   - It concurrently attempts to launch `AODActivity` with `FLAG_ACTIVITY_NEW_TASK` and `showWhenLocked=true`.
   - In parallel, it queries `GetThemesUseCase.getActiveTheme()` and instructs `AODWindowOverlayManager.showOverlay(theme)` to attach a `TYPE_APPLICATION_OVERLAY` full-screen window to `WindowManager`.

4. **Rendering & Refresh Phase:**
   - `AODRenderView` is attached, starts a 1Hz `Handler` redraw loop (`redrawRunnable`), and subscribes to `NotificationRepository`, `BatteryRepository`, and `MediaRepository`.
   - `AODRenderer` clears the canvas to AMOLED pure black (`#000000`), calculates periodic pixel shifts via `BurnInManager`, applies micro-animations via `AnimationEngine`, and delegates to individual element renderers (`Clock`, `Date`, `Battery`, `Music`, `Notification`, `Shape`, `Text`).

5. **Screen-On & Unlock Phase:**
   - On `ACTION_SCREEN_ON`, `AODForegroundService.onScreenOn()` re-arms the WakeLock.
   - On `ACTION_USER_PRESENT` (keyguard unlocked), `AODForegroundService.onUserPresent()` removes the overlay window and releases the WakeLock.

---

## 2. Comprehensive Bug & Issue Breakdown

---

### Issue 1: Dual Conflicting Display Rendering Engine Collision (`AODActivity` vs `AODWindowOverlayManager`)

* **Severity:** Critical
* **Exact File & Location:** `app/src/main/java/com/aodstudio/app/aod/service/AODForegroundService.kt` (Lines 141–166)
* **Root Cause:** In `onScreenOff()`, both `startActivity(aodIntent)` and `overlayManager.showOverlay(activeTheme)` are executed in parallel on every screen-off event.
* **How the Bug Can Occur:**
  1. The user locks the device -> `ACTION_SCREEN_OFF` is triggered.
  2. `AODForegroundService` starts `AODActivity` (an Activity with `showWhenLocked=true`) AND attaches `AODWindowOverlayManager` (a `TYPE_APPLICATION_OVERLAY` window).
  3. Two separate `AODRenderView` instances are instantiated and run simultaneously in memory, each executing its own 1Hz render loop.
  4. The system overlay sits on top of `AODActivity`, intercepting and consuming touch events.
  5. If the user double-taps to dismiss the overlay, only `AODWindowOverlayManager.hideOverlay()` is executed; `AODActivity` continues running underneath. Conversely, if `AODActivity` is finished, the overlay remains visible.
* **Expected Behavior:** A unified single-window architecture. Either use `AODActivity` with `showWhenLocked` or `AODWindowOverlayManager` with `TYPE_APPLICATION_OVERLAY`, coordinated by a state machine that guarantees strictly one active view hierarchy.
* **Actual Behavior:** Duplicate views, doubled resource/battery consumption, z-order fighting, and broken dismissal state.
* **Scope:** General Android Behavior.

---

### Issue 2: Full-Screen Touch Interception & User Lockout Vulnerability

* **Severity:** Critical
* **Exact File & Location:** `app/src/main/java/com/aodstudio/app/aod/overlay/AODWindowOverlayManager.kt` (Lines 84–123, 167–198)
* **Root Cause:** `AODWindowOverlayManager` attaches a `TYPE_APPLICATION_OVERLAY` window with `PixelFormat.OPAQUE` covering the entire screen (`MATCH_PARENT`) and sets an `OnTouchListener` that unconditionally returns `true`.
* **How the Bug Can Occur:**
  1. A user disables "Double Tap to Exit" in Settings (`setDoubleTapToExit(false)`).
  2. The screen turns off and the overlay attaches over the system lock screen.
  3. When the user attempts to swipe up or tap to enter their PIN/Pattern, the `OnTouchListener` intercepts and consumes all touch events.
  4. Because `getDoubleTapToExitSync()` returns `false`, double-tap is ignored.
  5. The user cannot dismiss the overlay, cannot reveal the system keyguard, and is permanently locked out of their device until force rebooted.
* **Expected Behavior:** 
  - Touches outside interactive widgets (e.g. media controls) should either pass through to underlying windows (via `FLAG_NOT_TOUCH_MODAL` or selective touch dispatching) or dismiss the overlay on swipe/tap to reveal the system lock screen.
  - A fallback exit gesture (such as single tap or swipe) must always be available.
* **Actual Behavior:** The overlay window traps all touch input, preventing access to the lock screen PIN, pattern, and system UI.
* **Scope:** General Android Behavior.

---

### Issue 3: Android 10+ / 14+ Background Activity Launch (BAL) Block

* **Severity:** Critical
* **Exact File & Location:** `app/src/main/java/com/aodstudio/app/aod/service/AODForegroundService.kt` (Lines 147–155)
* **Root Cause:** Calling `startActivity(aodIntent)` from a background Service when the screen is off violates Android's Background Activity Launch (BAL) security restrictions.
* **How the Bug Can Occur:**
  - Starting in Android 10 (API 29) and heavily restricted in Android 14/15/16 (API 34–36), background services cannot launch activities unless specific exemptions apply (such as `ActivityOptions.setPendingIntentBackgroundActivityStartMode(MODE_BACKGROUND_ACTIVITY_START_ALLOWED)`).
  - When `onScreenOff()` runs while the app is in the background, Android blocks the intent with log message: `Background activity start [com.aodstudio.app] was blocked`.
  - `AODActivity` fails to launch silently.
* **Expected Behavior:** The activity launch intent should be started using an authorized pending intent with background activity launch privileges, or rely on the `SYSTEM_ALERT_WINDOW` overlay pipeline where background activity starts are restricted.
* **Actual Behavior:** Android's security sandbox silences the background activity start, leaving `AODActivity` unlaunched.
* **Scope:** General Android Behavior (Enforced on Android 10+; tightened in Android 14+).

---

### Issue 4: `SecurityException` Crash in `AODActivity` on Android 14+ (Missing Receiver Flag)

* **Severity:** Critical
* **Exact File & Location:** `app/src/main/java/com/aodstudio/app/aod/ui/AODActivity.kt` (Lines 116–117)
* **Root Cause:** Calling `registerReceiver(userPresentReceiver, filter)` without specifying `Context.RECEIVER_EXPORTED` or `Context.RECEIVER_NOT_EXPORTED` on apps targeting Android 14+ (API 34+).
* **How the Bug Can Occur:**
  - When `AODActivity` is instantiated on Android 14, 15, or 16:
  - Line 117 executes: `registerReceiver(userPresentReceiver, filter)`.
  - Android 14+ throws `java.lang.SecurityException: com.aodstudio.app: One of RECEIVER_EXPORTED or RECEIVER_NOT_EXPORTED should be specified`.
  - The application crashes immediately upon entering `onCreate()`.
* **Expected Behavior:** Dynamic receiver registration must pass the proper flag on API 33+:
  ```kotlin
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      registerReceiver(userPresentReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
  } else {
      registerReceiver(userPresentReceiver, filter)
  }
  ```
* **Actual Behavior:** Instant fatal runtime crash on Android 14+ devices.
* **Scope:** General Android Behavior.

---

### Issue 5: `SecurityException` Crash on Task Removal in `ServiceWatchdog` (Missing Exact Alarm Permissions)

* **Severity:** Critical
* **Exact File & Location:** `app/src/main/java/com/aodstudio/app/aod/lifecycle/ServiceWatchdog.kt` (Lines 47–59) & `app/src/main/AndroidManifest.xml`
* **Root Cause:** `ServiceWatchdog.schedule()` invokes `alarmManager.setExactAndAllowWhileIdle()` without declaring `SCHEDULE_EXACT_ALARM` or `USE_EXACT_ALARM` in `AndroidManifest.xml` and without checking `alarmManager.canScheduleExactAlarms()`.
* **How the Bug Can Occur:**
  1. The user clears the app from Recents or OriginOS terminates the task.
  2. `AODForegroundService.onTaskRemoved()` fires and invokes `ServiceWatchdog.schedule(this)`.
  3. On Android 12+ (API 31+), invoking `setExactAndAllowWhileIdle()` without exact alarm permissions throws: `SecurityException: Caller com.aodstudio.app needs to hold android.permission.SCHEDULE_EXACT_ALARM or android.permission.USE_EXACT_ALARM`.
  4. The crash prevents the watchdog from arming, terminating the restart flow.
* **Expected Behavior:** `ServiceWatchdog` should check `alarmManager.canScheduleExactAlarms()` before calling exact alarm APIs, fallback to `setAndAllowWhileIdle()` (inexact) on rejection, and declare the permission in `AndroidManifest.xml`.
* **Actual Behavior:** Unhandled fatal exception on task removal.
* **Scope:** General Android Behavior.

---

### Issue 6: Stale / Cached Active Theme on Running AOD Service & Ignored Apply Calls

* **Severity:** High
* **Exact File & Location:** `app/src/main/java/com/aodstudio/app/aod/service/AODForegroundService.kt` (Lines 102–105), `app/src/main/java/com/aodstudio/app/feature/preview/AODPreviewViewModel.kt` (Lines 52–64)
* **Root Cause:** `AODForegroundService.onStartCommand()` is a no-op that only returns `START_STICKY`. It does not parse incoming intent actions or re-query `getThemesUseCase.getActiveTheme()`.
* **How the Bug Can Occur:**
  1. `AODForegroundService` is already running with Theme A.
  2. The user opens `AODPreviewScreen`, selects Theme B, and clicks "Apply to AOD Screen".
  3. `AODPreviewViewModel` sets Theme B as active in DataStore and calls `AODForegroundService.startService(context)`.
  4. Because the service is already running, `onCreate()` does not run; `onStartCommand()` is invoked with the intent.
  5. `onStartCommand()` does nothing.
  6. When the screen turns off, if `AODActivity` is already cached or the overlay was loaded, it continues to display Theme A.
  7. If `AODActivity` is brought to front with `FLAG_ACTIVITY_REORDER_TO_FRONT`, `AODActivity.onNewIntent()` is missing, so `loadActiveTheme()` never executes again.
* **Expected Behavior:** `onStartCommand()` should accept an action (e.g. `ACTION_RELOAD_THEME` or `EXTRA_THEME_ID`), reload the active theme, and update `AODWindowOverlayManager` / `AODActivity` in real-time.
* **Actual Behavior:** The user is told "Theme applied to AOD", but the off-screen display renders the previous theme until the entire app process is killed and restarted.
* **Scope:** General Android Behavior.

---

### Issue 7: One-Way Suppression Flaw & 24/7 Battery Drain in `PocketSensorManager`

* **Severity:** High
* **Exact File & Location:** `app/src/main/java/com/aodstudio/app/aod/sensor/PocketSensorManager.kt` (Lines 29–54), `app/src/main/java/com/aodstudio/app/aod/service/AODForegroundService.kt` (Lines 248–255)
* **Root Cause:** 
  1. `AODForegroundService.startPocketDetection()` only handles `if (isInPocket)` (hiding the overlay), with no `else` branch to restore the overlay when taken out of the pocket.
  2. `PocketSensorManager.startListening()` is started in `AODForegroundService.onCreate()` and runs continuously 24/7 even when the device screen is fully ON and being actively used.
* **How the Bug Can Occur:**
  1. Device is placed into a pocket -> proximity sensor detects object -> `overlayManager.hideOverlay()` is called.
  2. Device is taken out of pocket -> `isInPocket` becomes `false` -> nothing is called. The screen remains off/blank, failing to show the AOD display.
  3. The proximity sensor hardware listener remains active indefinitely, preventing the CPU from entering deep sleep and draining battery while the user interacts with other applications.
* **Expected Behavior:** 
  - Sensor listening should only be active while `isScreenOff` is true.
  - When `isInPocket == false` during screen-off, the AOD overlay should be re-attached automatically.
* **Actual Behavior:** One-way permanent suppression of the AOD overlay and continuous background sensor battery drain.
* **Scope:** General Android Behavior.

---

### Issue 8: Unreliable Service Status via Deprecated API & Missing `isAodEnabled` Persistence

* **Severity:** High
* **Exact File & Location:** `app/src/main/java/com/aodstudio/app/feature/home/HomeViewModel.kt` (Lines 61–66), `app/src/main/java/com/aodstudio/app/feature/settings/SettingsViewModel.kt` (Lines 194–199), `app/src/main/java/com/aodstudio/app/aod/lifecycle/BootReceiver.kt` (Lines 21–37)
* **Root Cause:** Both ViewModels determine if AOD is active using `ActivityManager.getRunningServices()`, which has been deprecated since API 26 and returns incomplete or empty lists on modern Android versions. Furthermore, there is no persistent preference for the user's master AOD enable/disable state.
* **How the Bug Can Occur:**
  1. The user enables AOD in Settings.
  2. On app cold start or when the service process is briefly recycled, `getRunningServices()` returns empty. `HomeViewModel` and `SettingsViewModel` display "AOD is inactive" and toggle the switch to OFF.
  3. On device reboot, `BootReceiver` runs `AODForegroundService.startService(context)` unconditionally, even if the user explicitly turned AOD OFF before restarting.
* **Expected Behavior:** A persistent `isAodEnabled` Boolean preference must be stored in DataStore/SharedPreferences. UI states and `BootReceiver` must query this persistent source of truth rather than querying deprecated transient process lists.
* **Actual Behavior:** Inconsistent UI state across app launches and unwanted service auto-starts on reboot when disabled by the user.
* **Scope:** General Android Behavior.

---

### Issue 9: Double Scaling Bug in Element Renderers (Squared Scale Factor)

* **Severity:** Medium
* **Exact File & Location:** `app/src/main/java/com/aodstudio/app/aod/renderer/AODRenderer.kt` (Lines 83–86), `app/src/main/java/com/aodstudio/app/aod/renderer/renderers/MusicElementRenderer.kt` (Line 37), `app/src/main/java/com/aodstudio/app/aod/renderer/renderers/NotificationElementRenderer.kt` (Line 39)
* **Root Cause:** `AODRenderer.renderTheme()` applies `canvas.scale(totalScale, totalScale, drawX, drawY)` to the Canvas transformation matrix (where `totalScale = element.scale * animProps.scaleMultiplier`). However, sub-renderers like `MusicElementRenderer` and `NotificationElementRenderer` ALSO compute `val scale = context.scaleFactor * element.scale` and scale their internal bounds and text sizes by it.
* **How the Bug Can Occur:** When a user sets the scale of a Music or Notification element in the editor to `1.5f`:
  - Canvas matrix scales the drawing by `1.5f`.
  - The renderer internally multiplies sizes, icon bounds, and stroke widths by `1.5f`.
  - The resulting output is scaled by `1.5 * 1.5 = 2.25f` (scale squared), overflowing the screen and distorting typography and alignment.
* **Expected Behavior:** `ElementRenderer` implementations should scale geometry using `context.scaleFactor` only, letting `canvas.scale()` apply the element's individual scale multiplier uniformly.
* **Actual Behavior:** Quadratic scaling distortion when element scale is modified in the theme editor.
* **Scope:** General Android Behavior.

---

### Issue 10: UI False Positives & Silent Failure on AOD Apply

* **Severity:** Medium
* **Exact File & Location:** `app/src/main/java/com/aodstudio/app/feature/editor/AODEditorViewModel.kt` (Lines 245–272), `app/src/main/java/com/aodstudio/app/feature/library/ThemeLibraryViewModel.kt` (Lines 108–132)
* **Root Cause:** ViewModels catch service start exceptions with generic `catch (e: Throwable)` and display success messages like `"Theme saved and active on AOD!"` without validating whether the service actually started.
* **How the Bug Can Occur:** On Android 14+, if `startForegroundService()` throws a `ForegroundServiceStartNotAllowedException` due to background execution restrictions, the exception is swallowed by the ViewModel's catch block, and the UI reports success.
* **Expected Behavior:** The UI should verify service execution state and display an error banner if the foreground service cannot be started.
* **Actual Behavior:** The user is misled into believing AOD is active when it failed silently.
* **Scope:** General Android Behavior.

---

### Issue 11: Deprecated WakeLock Type & Display State Fighting on `SCREEN_ON`

* **Severity:** Medium
* **Exact File & Location:** `app/src/main/java/com/aodstudio/app/aod/service/AODForegroundService.kt` (Lines 200–232, 168–181)
* **Root Cause:** `PowerManager.FULL_WAKE_LOCK` with `ACQUIRE_CAUSES_WAKEUP` is deprecated since API 17. In `onScreenOff()`, acquiring it immediately forces the display back to `STATE_ON`. In `onScreenOn()`, the service retains the opaque overlay instead of allowing the user to view their lock screen notifications and status.
* **How the Bug Can Occur:**
  1. User presses power button to turn screen off -> `ACTION_SCREEN_OFF` fires.
  2. `acquireWakeLock()` with `ACQUIRE_CAUSES_WAKEUP` forces screen on immediately with the AOD overlay.
  3. When user presses power button again to check their lock screen -> `ACTION_SCREEN_ON` fires.
  4. `onScreenOn()` keeps the black overlay attached, obscuring system notifications, battery bar, and lockscreen clock until the user completes authentication (`ACTION_USER_PRESENT`).
* **Expected Behavior:** The overlay should use `FLAG_KEEP_SCREEN_ON` directly on the window attributes rather than legacy `FULL_WAKE_LOCK`, and properly handle transition between ambient AOD and active lock screen.
* **Actual Behavior:** The AOD overlay masks the native lock screen completely even after the user explicitly presses power to wake the device.
* **Scope:** General Android Behavior & OriginOS power management.

---

### Issue 12: Missing Runtime Permission Request for `POST_NOTIFICATIONS` (Android 13+)

* **Severity:** Medium
* **Exact File & Location:** `app/src/main/AndroidManifest.xml` (Line 26), `app/src/main/java/com/aodstudio/app/feature/settings/SettingsViewModel.kt` (Lines 50–66)
* **Root Cause:** `POST_NOTIFICATIONS` is declared in the manifest, but there is no runtime permission request logic anywhere in the app.
* **How the Bug Can Occur:** On Android 13+ (API 33+), foreground services must show a persistent notification. If notification permission is denied or revoked:
  - The foreground service notification is suppressed by the OS.
  - On Android 14+, running a `specialUse` foreground service without notification permissions may cause the OS to demote the service priority and terminate it under memory pressure.
* **Expected Behavior:** `SettingsViewModel` and onboarding flows should check `NotificationManagerCompat.from(context).areNotificationsEnabled()` and prompt the user to grant notification permissions.
* **Actual Behavior:** Notification permission is never requested at runtime.
* **Scope:** General Android Behavior (Android 13+).

---

### Issue 13: Memory Leaks in Coroutine Scopes & Unrecycled Bitmaps

* **Severity:** Low
* **Exact File & Location:** `app/src/main/java/com/aodstudio/app/aod/renderer/AODRenderView.kt` (Lines 70–85), `app/src/main/java/com/aodstudio/app/notification/service/AODNotificationListenerService.kt` (Lines 93–113)
* **Root Cause:** 
  1. `AODRenderView` creates a standalone `CoroutineScope` that is not cancelled when views are detached or recreated in `AODWindowOverlayManager.showOverlay()`.
  2. `AODNotificationListenerService` extracts full Bitmaps on every single notification update without caching or recycling previous bitmap allocations.
* **How the Bug Can Occur:** Prolonged background execution with frequent notifications leads to GC churn and memory fragmentation.
* **Expected Behavior:** `AODRenderView` coroutine scopes should be lifecycle-aware or tied to `ViewTreeLifecycleOwner`, and icon bitmaps should be cached by package name with LRU eviction.
* **Actual Behavior:** Orphaned coroutines and redundant bitmap allocations during long-running AOD sessions.
* **Scope:** General Android Behavior.

---

### Issue 14: Clean Install Template Discovery Race Condition

* **Severity:** Low
* **Exact File & Location:** `app/src/main/java/com/aodstudio/app/data/repository/ThemeRepositoryImpl.kt` (Lines 37–40), `app/src/main/java/com/aodstudio/app/data/local/ThemeStorage.kt` (Lines 33–44)
* **Root Cause:** In `ThemeRepositoryImpl.init`, `refreshThemes()` calls `themeStorage.getAllThemes()` directly instead of calling `themeStorage.initializeBuiltInThemesIfNeeded()`.
* **How the Bug Can Occur:** On the very first launch after installation when internal storage is empty, `getAllThemes()` discovers no files and falls back to writing only the first template (`Minimal Orbit`). The remaining built-in templates (Analog, Neon, Retro, etc.) are skipped until a separate call explicitly invokes `getAllThemes()`.
* **Expected Behavior:** `ThemeRepositoryImpl.init` should invoke `initializeBuiltInThemesIfNeeded()` to guarantee all templates are populated on first launch.
* **Actual Behavior:** Initial theme stream emits a partial list containing only 1 theme on fresh installs.
* **Scope:** General Android Behavior.

---

### Issue 15: Missing Repository Bindings in `AODPreviewScreen`

* **Severity:** Low
* **Exact File & Location:** `app/src/main/java/com/aodstudio/app/feature/preview/AODPreviewScreen.kt` (Lines 90–100)
* **Root Cause:** In `AODPreviewScreen`, `AODRenderView` is instantiated without attaching `BatteryRepository`, `NotificationRepository`, or `MediaRepository`.
* **How the Bug Can Occur:** When a user previews any theme containing Battery, Music, or Notification elements, the preview renders static default values instead of live or simulated data.
* **Expected Behavior:** `AODPreviewScreen` should inject repositories from its ViewModel and supply them to `AODRenderView`.
* **Actual Behavior:** Widgets render with uninitialized empty state in full preview mode.
* **Scope:** General Android Behavior.

---

## 3. Recommended Remediation Architecture

```
                                  USER APPLIES THEME
                                           │
                                           ▼
                            ┌──────────────────────────────┐
                            │    ThemeRepository &         │
                            │    SettingsRepository        │
                            │  (Persist activeThemeId &    │
                            │      isAodEnabled=true)      │
                            └──────────────┬───────────────┘
                                           │
                                           ▼
                            ┌──────────────────────────────┐
                            │    AODForegroundService      │
                            │ (START_STICKY + Watchdog)    │
                            └──────────────┬───────────────┘
                                           │
                        ┌──────────────────┴──────────────────┐
                        ▼                                     ▼
             SCREEN_OFF Broadcast                   ACTION_RELOAD_THEME
                        │                                     │
                        ▼                                     ▼
        ┌────────────────────────────────┐            Update Active
        │     Unified AOD Controller     │ ◄───────── Theme Flow
        │   - Acquire FLAG_KEEP_SCREEN   │
        │   - Display Single Window View │
        │   - DoubleTap / Keyguard Aware │
        └────────────────────────────────┘
```

1. **Unify Window Management:** Eliminate dual rendering by consolidating all off-screen display logic into a single dedicated component (`AODWindowOverlayManager` or `AODActivity` with explicit intent flags, but never both simultaneously).
2. **Implement Safe Touch Passthrough:** Ensure the overlay provides an unobstructed fallback to the system lock screen on tap/swipe, preventing user lockouts.
3. **Add Android 14+ Receiver Flags & Exact Alarm Checks:** Register all receivers with `RECEIVER_NOT_EXPORTED` and guard `ServiceWatchdog` with `canScheduleExactAlarms()`.
4. **Persist Master AOD State:** Store `isAodEnabled` in DataStore to eliminate dependency on deprecated `getRunningServices()`.
5. **Reactive Theme Updates:** Implement an `ACTION_RELOAD_THEME` command in `AODForegroundService.onStartCommand()` to update running AOD views immediately upon theme changes.
