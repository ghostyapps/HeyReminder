package com.dcapps.heyreminder

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BatteryStatusViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val preferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(BatteryUiState())
    val uiState: StateFlow<BatteryUiState> = _uiState.asStateFlow()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            updateBatteryState(intent)
        }
    }

    init {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val stickyIntent = ContextCompat.registerReceiver(
            appContext,
            batteryReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        if (stickyIntent != null) {
            updateBatteryState(stickyIntent)
        }
    }

    override fun onCleared() {
        runCatching { appContext.unregisterReceiver(batteryReceiver) }
        super.onCleared()
    }

    private fun updateBatteryState(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

        if (level < 0 || scale <= 0) return

        val percentage = (level * 100) / scale
        val isFull = status == BatteryManager.BATTERY_STATUS_FULL || percentage >= 100

        if (isFull) {
            saveLastFullChargeTime(System.currentTimeMillis())
        }

        val lastFullCharge = preferences.getLong(KEY_LAST_FULL_CHARGE, -1L).takeIf { it > 0L }
        val estimatedRemaining = calculateEstimatedRemainingTime(lastFullCharge, percentage)

        _uiState.value = BatteryUiState(
            batteryPercentage = percentage,
            lastFullChargeTimestamp = lastFullCharge,
            estimatedRemainingTimeMillis = estimatedRemaining
        )
    }

    private fun saveLastFullChargeTime(timestamp: Long) {
        preferences.edit().putLong(KEY_LAST_FULL_CHARGE, timestamp).apply()
    }

    private fun calculateEstimatedRemainingTime(lastFullCharge: Long?, percentage: Int): Long? {
        if (lastFullCharge == null) return null
        if (percentage <= 0 || percentage >= 100) return null

        val elapsed = System.currentTimeMillis() - lastFullCharge
        if (elapsed <= 0) return null

        val consumedPercent = 100 - percentage
        if (consumedPercent <= 0) return null

        return (elapsed * percentage) / consumedPercent
    }

    companion object {
        private const val PREFS_NAME = "battery_prefs"
        private const val KEY_LAST_FULL_CHARGE = "last_full_charge"
    }
}

data class BatteryUiState(
    val batteryPercentage: Int = 0,
    val lastFullChargeTimestamp: Long? = null,
    val estimatedRemainingTimeMillis: Long? = null
)
