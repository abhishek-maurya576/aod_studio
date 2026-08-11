# Technical Q&A — AOD Studio Architecture & Display Lifecycle Diagnosis

## 1. Project Architecture & Access Level

* **Are you building a standard user app (APK), or do you have system/root access?**
  - **Answer:** Standard third-party user application (`APK`). Compiled with standard Android SDK tools (Gradle, Hilt, Jetpack Compose, AGP 9.0+) without root or `platform` signature permissions.

* **If you are building a system-level feature, are you modifying the AOSP SystemUI source code, or are you working directly with a custom Linux kernel / Hardware Abstraction Layer (HAL)?**
  - **Answer:** No. We are working at the standard Android application framework layer (`android.view.WindowManager`, `android.app.Service`, `android.graphics.Canvas`).

---

## 2. Display State & Lifecycle Handling

* **How are you handling the power button press? Are you intercepting the broadcast ACTION_SCREEN_OFF, or are you using a custom display power state machine?**
  - **Answer:** We intercept `Intent.ACTION_SCREEN_OFF`, `Intent.ACTION_SCREEN_ON`, and `Intent.ACTION_USER_PRESENT` using a dynamic `BroadcastReceiver` (`Context.RECEIVER_NOT_EXPORTED`) registered in `AODForegroundService.kt`.

* **What specific Display State are you targeting when the screen goes dark? Are you trying to force the screen into Display.STATE_DOZE_SUSPEND, or are you attempting to draw while the system thinks it is in STATE_OFF?**
  - **Answer:** We cannot target `Display.STATE_DOZE` or `Display.STATE_DOZE_SUSPEND` directly because `android.permission.CONTROL_DISPLAY_DOZE` is restricted to system-privileged apps (`signature|privileged`).
  - Currently, when `ACTION_SCREEN_OFF` fires, we acquire a `PowerManager.PARTIAL_WAKE_LOCK` and attach a `TYPE_APPLICATION_OVERLAY` window with `screenBrightness = 0.01f`, `FLAG_TURN_SCREEN_ON`, `FLAG_KEEP_SCREEN_ON`, and `FLAG_SHOW_WHEN_LOCKED`.

---

## 3. Rendering Method

* **What are you using to draw the AOD interface? For example, are you using a standard Android View/Canvas, a system overlay window (WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY), or a low-level native surface like SurfaceControl?**
  - **Answer:** Standard Android `Canvas` rendering inside a custom subclassed `View` (`AODRenderView.kt`), added to `WindowManager` via `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`.

* **When the screen switches off, does your canvas rendering throw an error (like an invalid surface exception), or does the code run in the background but the physical screen just stays pitch black?**
  - **Answer:** No exception is thrown (`AODRenderView.onDraw()` loop continues executing silently). The physical screen stays pitch black or shows the OEM default lockscreen/system AOD because:
    1. OriginOS / Android 16 hardware display driver forces `Display.STATE_OFF` at the kernel level when the power key is pressed, ignoring 3rd-party `FLAG_KEEP_SCREEN_ON`.
    2. OriginOS System Keyguard (`TYPE_KEYGUARD_DIALOG` / `TYPE_DISPLAY_OVERLAY`) renders above 3rd-party `TYPE_APPLICATION_OVERLAY` windows on the lockscreen.

---

## 4. Hardware Target

* **What device or emulator are you testing this on? Does the target hardware actually feature an OLED panel that supports low-power states, or are you testing on an LCD screen?**
  - **Answer:** Physical Hardware Device — **Vivo T4 Pro (Model V2510)** running **Android 16 (API 36)** with **OriginOS 6**.
  - **Panel Type:** Hardware AMOLED display supporting zero-subpixel black rendering (`#000000`).
