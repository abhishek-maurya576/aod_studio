# AOD Studio — Technical Feasibility Report

**Date:** 2026-08-10
**Target Device:** Vivo T4 Pro
**Target OS:** OriginOS 6 / Android 16 (API 36+)
**Author:** AI Agent (Lead Android Engineer)

---

## Confirmed

| # | Finding | Details |
|---|---------|---------|
| ✓ | `TYPE_APPLICATION_OVERLAY` | Can draw over lock screen with `SYSTEM_ALERT_WINDOW` permission. Not a true system AOD — sits below critical system windows. |
| ✓ | `NotificationListenerService` | Official API for notification events (posted/removed). Stable since API 18. Requires user to manually enable in Settings. |
| ✓ | `MediaSession` API | Provides media playback info: title, artist, artwork, progress, duration, play state. Requires `MEDIA_CONTENT_CONTROL` or notification listener access. |
| ✓ | Foreground Service | Required for persistent background work on Android 14+/16. Must declare valid `foregroundServiceType` and permissions. |
| ✓ | `ACTION_SCREEN_OFF` / `ACTION_SCREEN_ON` | Must be registered **dynamically** (not in manifest). Works from a running Service. |
| ✓ | Battery BroadcastReceiver | `ACTION_BATTERY_CHANGED` is stable. Provides level, charging status, plugged state. |
| ✓ | AMOLED Black (#000000) | True black pixels are OFF on AMOLED = significant battery saving. Must use `Color.Black` exactly, not near-black. |
| ✓ | Canvas rendering | Compose `Canvas` and custom `View.onDraw()` both support hardware-accelerated rendering. Cache `Paint`/`Path` objects for performance. |
| ✓ | `FLAG_SHOW_WHEN_LOCKED` | Allows Activity/overlay to appear over the lock screen. |
| ✓ | Boot receiver | `RECEIVE_BOOT_COMPLETED` can restart the AOD service after reboot. |
| ✓ | Android 16 FGS changes | FGS now subject to job execution quotas. Must declare valid `foregroundServiceType`. Google Play requires API 36+ by Aug 31, 2026. |

## Unconfirmed (Requires Physical Device Testing on Vivo T4 Pro)

| # | Question | Test Method |
|---|----------|-------------|
| ? | Does Vivo OriginOS 6 kill the overlay service after prolonged screen-off? | Run FGS + overlay for 1h/8h screen-off, check if alive. |
| ? | Does Vivo's native AOD block/conflict with third-party overlays? | Test with native AOD ON and OFF. |
| ? | Does the overlay remain visible when screen is truly OFF (vs ambient)? | Observe display behavior after `ACTION_SCREEN_OFF`. |
| ? | Exact autostart behavior on Vivo T4 Pro | Test service restart after force-stop, reboot, battery optimization. |
| ? | Does Vivo's battery optimization kill the FGS despite user whitelisting? | Whitelist app, run overnight, check survival. |
| ? | Display refresh rate during overlay when locked | Profile FPS and power draw. |
| ? | Does `SYSTEM_ALERT_WINDOW` require special Vivo-specific grant flow? | Test permission request flow on Vivo. |
| ? | iManager interference with background service | Check if iManager has separate kill logic. |

## Rejected

| # | Approach | Reason |
|---|----------|--------|
| ✗ | `AccessibilityService` for AOD | Violates Play Store policies. Unnecessary for this use case. |
| ✗ | Native AOD system APIs | No public API exists for third-party apps. OEM-only. |
| ✗ | `TYPE_SYSTEM_OVERLAY` | Deprecated. Not available to non-system apps. |
| ✗ | Hardware display controller access | OEM-only. Cannot control OLED refresh rate or panel dimming. |
| ✗ | `DevicePolicyManager` for lock screen | Requires device admin, invasive, not appropriate. |
| ✗ | Continuous 60fps redraw loop | Catastrophic battery drain. Violates project rules. |
| ✗ | `BODY_SENSORS` permission | Not needed for AOD. |

---

## Chosen Architecture

```
┌──────────────────────────────────────────────────┐
│                  AODController                    │
│                                                   │
│  ┌───────────────────────────────────────────┐    │
│  │        AODForegroundService                │    │
│  │  (foregroundServiceType = specialUse)      │    │
│  │                                            │    │
│  │  ┌──────────────────────────────────────┐  │    │
│  │  │  BroadcastReceiver                   │  │    │
│  │  │  ACTION_SCREEN_OFF → show overlay    │  │    │
│  │  │  ACTION_SCREEN_ON  → hide overlay    │  │    │
│  │  │  ACTION_USER_PRESENT → dismiss       │  │    │
│  │  └──────────────────────────────────────┘  │    │
│  │                                            │    │
│  │  ┌──────────────────────────────────────┐  │    │
│  │  │  WindowManager                       │  │    │
│  │  │  TYPE_APPLICATION_OVERLAY            │  │    │
│  │  │  FLAG_SHOW_WHEN_LOCKED               │  │    │
│  │  │  FLAG_NOT_FOCUSABLE                  │  │    │
│  │  │  MATCH_PARENT × MATCH_PARENT         │  │    │
│  │  └──────────────────────────────────────┘  │    │
│  │                                            │    │
│  │  ┌──────────────────────────────────────┐  │    │
│  │  │  AODRenderView (Custom View)         │  │    │
│  │  │  Canvas-based renderer               │  │    │
│  │  │  #000000 background (AMOLED)         │  │    │
│  │  │  Low-frequency redraw (1/min idle)   │  │    │
│  │  └──────────────────────────────────────┘  │    │
│  └───────────────────────────────────────────┘    │
│                                                   │
│  ┌───────────────────────────────────────────┐    │
│  │  Data Providers                            │    │
│  │  • BatteryRepository (BroadcastReceiver)   │    │
│  │  • NotificationRepository (Listener)       │    │
│  │  • MediaRepository (MediaSession)          │    │
│  │  • TimeProvider (System clock)             │    │
│  └───────────────────────────────────────────┘    │
│                                                   │
│  ┌───────────────────────────────────────────┐    │
│  │  Vivo Compatibility Layer                  │    │
│  │  • Autostart guidance                      │    │
│  │  • Battery optimization whitelist          │    │
│  │  • iManager workarounds (if needed)        │    │
│  └───────────────────────────────────────────┘    │
└──────────────────────────────────────────────────┘
```

### Rendering Strategy

- **Idle clock (no seconds):** Redraw every **60 seconds**
- **Clock with seconds:** Redraw every **1 second**
- **Animation active:** Redraw at **10-15 FPS** max (not 60)
- **Battery saver mode:** Disable animations, redraw every 60 seconds only
- **Burn-in protection:** Shift position every **5 minutes** with ≤4px offset

### Battery Impact Disclaimer

This is a **simulated AOD** using an overlay, not a native OEM AOD. Key differences:
- Screen remains "on" at minimum brightness (not truly off)
- Cannot control OLED panel refresh rate
- Higher battery consumption than native Samsung/Pixel AOD
- Must be clearly communicated to users in onboarding

---

## Permissions Required

| Permission | Purpose | When Requested |
|------------|---------|----------------|
| `SYSTEM_ALERT_WINDOW` | Draw AOD overlay | When user enables AOD |
| `FOREGROUND_SERVICE` | Keep service alive | App start (manifest) |
| `FOREGROUND_SERVICE_SPECIAL_USE` | FGS type for AOD | App start (manifest) |
| `RECEIVE_BOOT_COMPLETED` | Restart after reboot | App start (manifest) |
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Read notifications | When user enables notifications on AOD |
| `POST_NOTIFICATIONS` | Service notification | When AOD service starts |

---

## Vivo T4 Pro — Device Testing Checklist

Run these tests on the physical device before M13:

- [ ] Overlay appears when screen locks
- [ ] Overlay disappears on unlock
- [ ] Service survives 1 hour screen-off
- [ ] Service survives 8 hours screen-off
- [ ] Service restarts after reboot
- [ ] Service survives battery optimization (whitelisted)
- [ ] Autostart works after force-stop
- [ ] No conflict with native Vivo AOD enabled
- [ ] No conflict with native Vivo AOD disabled
- [ ] Notification listener receives events
- [ ] Media session data accessible
- [ ] Battery percentage updates correctly
- [ ] Overlay permission grant flow works
- [ ] iManager does not kill service
- [ ] Battery drain measurement (1h, 8h)

---

## Exit Condition

> **How exactly will our AOD renderer become visible when the Vivo T4 Pro screen is locked/off?**

**Answer (pending device validation):**
A Foreground Service registers a dynamic BroadcastReceiver for `ACTION_SCREEN_OFF`. When triggered, it adds a `TYPE_APPLICATION_OVERLAY` window with `FLAG_SHOW_WHEN_LOCKED` to the WindowManager. The overlay contains our custom `AODRenderView` with a pure black (#000000) background. The overlay is removed on `ACTION_USER_PRESENT` (unlock).

This approach is the standard mechanism used by third-party AOD apps on Android. Whether it works reliably on Vivo OriginOS 6 specifically requires physical device testing (see checklist above).
