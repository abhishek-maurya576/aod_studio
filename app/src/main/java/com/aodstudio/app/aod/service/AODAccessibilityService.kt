package com.aodstudio.app.aod.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.aodstudio.app.config.AppConfig

/**
 * AODAccessibilityService — alternative screen-intercept path.
 *
 * ## PURPOSE
 * On some Vivo OriginOS builds, ACTION_SCREEN_OFF is delivered AFTER the display has
 * already transitioned to STATE_OFF, making the WakeLock + overlay path in
 * [AODForegroundService] a race condition that may lose. An AccessibilityService can
 * receive window-state events synchronously in the UI pipeline and potentially catch
 * the power-button press before STATE_OFF is committed.
 *
 * ## FEATURE FLAG
 * Gated by [AppConfig.Features.USE_ACCESSIBILITY_SCREEN_INTERCEPT] (default: false).
 * Enable it, test on the physical Vivo T4 Pro, and compare which path keeps the
 * screen alive more reliably before enabling for users.
 *
 * ## PERMISSION COST
 * Requires BIND_ACCESSIBILITY_SERVICE — a "sensitive" permission that:
 * - Triggers a Play Store review and a mandatory user-facing accessibility settings dialog.
 * - Is visible in the Accessibility settings as a running service (user-visible overhead).
 * This is why it defaults to false. Only enable if the SCREEN_OFF broadcast path proves
 * insufficient on the target device.
 *
 * ## UNVERIFIED
 * It is not confirmed whether TYPE_WINDOW_STATE_CHANGED or any other event fires
 * before the display power state is committed on OriginOS 6. This must be tested
 * on the physical device. The service only logs events in this initial implementation;
 * actual interception logic is a TODO pending device test results.
 *
 * ## MANIFEST NOTE
 * This service is declared in AndroidManifest.xml only when
 * [AppConfig.Features.USE_ACCESSIBILITY_SCREEN_INTERCEPT] == true.
 * To enable: uncomment the <service> block in AndroidManifest.xml and set the flag to true.
 */
class AODAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        if (!AppConfig.Features.USE_ACCESSIBILITY_SCREEN_INTERCEPT) {
            Log.w(TAG, "USE_ACCESSIBILITY_SCREEN_INTERCEPT is false — service connected but passive")
            return
        }

        // Configure to receive the broadest event set to maximize interception window.
        // UNVERIFIED — test on device which event type fires earliest relative to power-button.
        serviceInfo = serviceInfo?.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOWS_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 0 // Receive events with no debounce delay
        }
        Log.i(TAG, "AccessibilityService connected — monitoring window state changes")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!AppConfig.Features.USE_ACCESSIBILITY_SCREEN_INTERCEPT) return
        // TODO: Implement actual screen-intercept logic once device testing confirms
        // which event type fires before STATE_OFF on OriginOS 6.
        // Candidate signals to investigate on device:
        //   - event.packageName == "com.android.systemui" with specific class names
        //   - event.eventType == TYPE_WINDOW_STATE_CHANGED to "PowerUI" component
        //   - KeyEvent.ACTION_DOWN KEYCODE_POWER via key event filter (requires FLAG_REQUEST_FILTER_KEY_EVENTS)
        // UNVERIFIED — do not ship this logic until confirmed working on T4 Pro.
        Log.v(TAG, "AccessibilityEvent: type=${event?.eventType}, pkg=${event?.packageName}")
    }

    override fun onInterrupt() {
        Log.w(TAG, "AccessibilityService interrupted")
    }

    companion object {
        private const val TAG = "AODAcessibilityService"
    }
}
