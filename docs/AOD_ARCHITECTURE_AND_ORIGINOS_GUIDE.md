# AOD Studio — Technical Architecture & OriginOS 6 Survival Guide

This document explains the technical architecture of **AOD Studio**, how "Fake AOD" works on modern Android versions (including Android 16 on Vivo OriginOS 6), why standard overlay approaches fail on the lock screen, and how we solved it.

---

## 💡 1. How Fake AOD Works: The Core Mechanism

### A. The Fundamental Constraint of Android AOD
True system-level Always-On Display puts the display controller into hardware `Display.STATE_DOZE` or `Display.STATE_DOZE_SUSPEND`, where the CPU sleeps and the panel updates at 1Hz or 10Hz via display co-processor.

**Why Third-Party APKs cannot do true AOD:**
Android requires `android.permission.CONTROL_DISPLAY_DOZE`, which is a `signature|privileged` permission only granted to pre-installed system apps signed with the device manufacturer's private platform key. Third-party APKs installed from Google Play or ADB cannot obtain this permission.

### B. The "Fake AOD" Solution
Instead of `STATE_DOZE`, we simulate AOD by:
1. Intercepting when the user locks or turns off the screen (`ACTION_SCREEN_OFF`).
2. Keeping the display in `STATE_ON` at **minimum possible backlight brightness** (`screenBrightness = 0.01f`).
3. Rendering a **pure black (`#000000`) background**. On AMOLED / OLED panels, subpixels showing `#000000` emit zero light and draw near-zero current.
4. Redrawing clock and notifications once per second (1Hz) or once per minute.

---

## 🔒 2. The Lock Screen Problem & How We Solved It

### The Mystery Bug You Experienced
When testing earlier versions:
- Screen locked → display went black / normal lockscreen showed (AOD invisible).
- Phone unlocked → suddenly the AOD screen appeared on top of the home screen!

### Why Did This Happen? (Android Security Rules)
Android OS (from API 26 through Android 16) has strict z-index security rules for system windows:
- `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY` windows created from a **Service** are placed by WindowManager **BELOW** the system Keyguard (Lock Screen).
- When the phone is locked, WindowManager hides or obscures `TYPE_APPLICATION_OVERLAY` behind the lock screen.
- When you unlocked the phone, Keyguard was dismissed, so the Service overlay suddenly became visible on the unlocked home screen!

### The Solution: `AODActivity` with `showWhenLocked="true"`
To render on top of a locked phone, Android **requires a dedicated Activity** configured with explicit lockscreen permissions.

1. **`AODActivity` Manifest Declaration:**
   ```xml
   <activity
       android:name=".aod.ui.AODActivity"
       android:exported="false"
       android:launchMode="singleInstance"
       android:showWhenLocked="true"
       android:turnScreenOn="true"
       android:excludeFromRecents="true"
       android:noHistory="true"
       android:theme="@style/Theme.AODStudio" />
   ```
   - `android:showWhenLocked="true"` instructs Android's `KeyguardController` that this Activity has permission to render **ABOVE** the system lock screen.
   - `android:turnScreenOn="true"` requests display power-on if it went to `STATE_OFF`.

2. **Window Flag Setup in `AODActivity.kt`:**
   ```kotlin
   // Native API 27+ lockscreen bypass
   setShowWhenLocked(true)
   setTurnScreenOn(true)

   // Keep screen on, minimum brightness
   window.addFlags(
       WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
       WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
       WindowManager.LayoutParams.FLAG_FULLSCREEN
   )

   val params = window.attributes
   params.screenBrightness = 0.01f
   window.attributes = params

   // Pure black background (#000000)
   setContentView(renderView)
   ```

3. **Foreground Service Interception (`AODForegroundService.kt`):**
   When `ACTION_SCREEN_OFF` fires:
   - Service acquires `PowerManager.FULL_WAKE_LOCK` (forces display `STATE_ON`).
   - Launches `AODActivity` with `FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_SINGLE_TOP`.
   - `AODActivity` immediately covers the screen with pure black and displays the dim AOD clock face.
   - When user authenticates (fingerprint / PIN / double-tap), `AODActivity.dismissAod()` calls `finishAndRemoveTask()`, returning the user smoothly to the unlocked home screen.

---

## 🛡️ 3. Vivo OriginOS 6 Background Survival Architecture

OEM Android skins like Vivo OriginOS 6 have aggressive background process killers that freeze or terminate third-party services. We implemented a 5-layer survival architecture:

```
┌─────────────────────────────────────────────────────────┐
│              Vivo OriginOS Survival Layers              │
├─────────────────────────────────────────────────────────┤
│ 1. Foreground Service (FOREGROUND_SERVICE_SPECIAL_USE)  │
│ 2. START_STICKY Service Lifecycle                       │
│ 3. AlarmManager Watchdog (ServiceWatchdog.kt)           │
│ 4. Vivo High Background Power Consumption Whitelist     │
│ 5. iManager Autostart Permission                        │
└─────────────────────────────────────────────────────────┘
```

### Layer 1: Foreground Service with `specialUse` Type
- `AODForegroundService` extends `Service()` (NOT `JobIntentService` or `WorkManager` which OriginOS freezes within 2 minutes).
- Manifest uses `android:foregroundServiceType="specialUse"` with Play Store justification property `android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE`.
- Uses an `IMPORTANCE_MIN` silent notification channel to avoid annoying the user while maintaining OS foreground classification.

### Layer 2: `PowerManager.FULL_WAKE_LOCK`
- `SCREEN_DIM_WAKE_LOCK` was confirmed via logcat to be a no-op on OriginOS 6 (the OS demoted it to CPU-only).
- Switched to `FULL_WAKE_LOCK` combined with `screenBrightness = 0.01f` to force the panel state while keeping power consumption minimal.

### Layer 3: AlarmManager Watchdog (`ServiceWatchdog.kt`)
- When OriginOS purges the app from Recents, `onTaskRemoved()` fires.
- `onTaskRemoved()` arms `ServiceWatchdog` via `AlarmManager.setExactAndAllowWhileIdle()`.
- Because `AlarmManager` alarms are dispatched by the Android `system_server` process (not the app process), it survives process freezing and restarts `AODForegroundService` automatically.

### Layer 4: Vivo OEM Compatibility Adapter (`VivoAdapter.kt`)
Provides version-gated deep-link intent chains with graceful fallback to standard App Info settings:

| OEM Requirement | Target Component / Intent Chain | Fallback |
|---|---|---|
| **High Background Power** | `com.vivo.abe.PermissionManagerActivity`<br>`com.vivo.abe.ui.BatteryManageListActivity` | `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` |
| **Autostart Permission** | `com.iqoo.secure.ui.phoneoptimize.BgStartUpManager`<br>`com.vivo.permissionmanager.activity.BgStartUpManagerActivity` | `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` |
| **Recents Lock** | Manual gesture instructions card (padlock icon in Recents screen) | N/A |

### Layer 5: Interactive 3-Step Vivo Onboarding (`VivoOnboardingScreen.kt`)
Animated Compose wizard that guides Vivo users on first launch to configure these 3 mandatory OEM settings so AOD never gets killed in the background.

---

## ⚡ 4. Rendering Performance & Battery Optimization

1. **Hardware Layer Caching (`AODRenderView.kt`):**
   ```kotlin
   setLayerType(LAYER_TYPE_HARDWARE, null)
   ```
   Tells HWUI/GPU to cache the View's drawing commands in VRAM between 1Hz ticks. Reduces CPU wake time during AOD redraws.

2. **Compositor OPAQUE Optimizations:**
   Window format set to `PixelFormat.OPAQUE`. Informs SurfaceFlinger to skip alpha blending with underlying windows.

3. **Pure Black Subpixel Power Draw:**
   Canvas background set to `Color.BLACK` (`#000000`). On AMOLED displays, black pixels turn off subpixel LEDs completely.

4. **Pocket Detection (`PocketSensorManager.kt`):**
   Uses `Sensor.TYPE_PROXIMITY`. When phone is face down or in a pocket, rendering loop pauses to preserve battery.

---

## 📝 Summary of Key Learnings

1. **Service Overlays cannot draw over Lock Screen on API 26+**: Never rely solely on `TYPE_APPLICATION_OVERLAY` for lockscreen features. Always use a dedicated `Activity` with `showWhenLocked="true"` and `turnScreenOn="true"`.
2. **`SCREEN_DIM_WAKE_LOCK` is unreliable on modern OEM ROMs**: On OriginOS 6 / Android 16, `SCREEN_DIM_WAKE_LOCK` is demoted. `FULL_WAKE_LOCK` + `screenBrightness = 0.01f` is required.
3. **Screen-Off Event Ordering**: `ACTION_SCREEN_ON` should NOT dismiss AOD if the keyguard is still active. Dismissal must occur on `ACTION_USER_PRESENT` (after authentication).
4. **AlarmManager is essential for OEM Doze survival**: `START_STICKY` is suppressed by OriginOS when cleared from Recents. `AlarmManager.setExactAndAllowWhileIdle` is required to guarantee service recovery.
