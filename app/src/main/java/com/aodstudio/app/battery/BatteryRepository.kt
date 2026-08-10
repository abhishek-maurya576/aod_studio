package com.aodstudio.app.battery

import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface for monitoring system battery state.
 */
interface BatteryRepository {

    /**
     * Observes live device battery status as a StateFlow.
     */
    val batteryState: StateFlow<BatteryState>

    /**
     * Returns the current snapshot of device battery state.
     */
    fun getCurrentBatteryState(): BatteryState
}
