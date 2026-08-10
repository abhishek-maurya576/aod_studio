package com.aodstudio.app.battery

/**
 * Data class representing live device battery state.
 */
data class BatteryState(
    val percentage: Int = 100,
    val isCharging: Boolean = false,
    val isFull: Boolean = false,
    val pluggedType: PluggedType = PluggedType.NONE,
    val health: String = "GOOD"
) {
    enum class PluggedType {
        NONE, AC, USB, WIRELESS
    }
}
