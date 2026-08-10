package com.aodstudio.app.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of BatteryRepository using Android system BroadcastReceiver.
 * Listens to Intent.ACTION_BATTERY_CHANGED for zero-polling, event-driven updates.
 */
@Singleton
class BatteryRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : BatteryRepository {

    private val _batteryState = MutableStateFlow(BatteryState())
    override val batteryState: StateFlow<BatteryState> = _batteryState.asStateFlow()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(cntx: Context?, intent: Intent?) {
            intent?.let { parseBatteryIntent(it) }
        }
    }

    init {
        registerBatteryReceiver()
    }

    override fun getCurrentBatteryState(): BatteryState {
        return _batteryState.value
    }

    private fun registerBatteryReceiver() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val stickyIntent = context.registerReceiver(batteryReceiver, filter)
        stickyIntent?.let { parseBatteryIntent(it) }
    }

    private fun parseBatteryIntent(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

        val pct = if (level >= 0 && scale > 0) {
            (level * 100f / scale).toInt()
        } else {
            100
        }

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        val isFull = status == BatteryManager.BATTERY_STATUS_FULL

        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val pluggedType = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> BatteryState.PluggedType.AC
            BatteryManager.BATTERY_PLUGGED_USB -> BatteryState.PluggedType.USB
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> BatteryState.PluggedType.WIRELESS
            else -> BatteryState.PluggedType.NONE
        }

        _batteryState.value = BatteryState(
            percentage = pct,
            isCharging = isCharging,
            isFull = isFull,
            pluggedType = pluggedType
        )
    }
}
