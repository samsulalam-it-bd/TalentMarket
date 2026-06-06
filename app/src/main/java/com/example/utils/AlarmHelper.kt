package com.example.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.util.Log

object AlarmHelper {
    private const val TAG = "AlarmHelper"

    /**
     * Schedules an exact alarm safely on any Android version from 5.0 (API 21) up to Android 15+ (API 35+).
     * If the SCHEDULE_EXACT_ALARM permission is missing on Android 12+ (API 31+), it falls back
     * gracefully to scheduling via setAndAllowWhileIdle() or set() to avoid crashing.
     */
    @SuppressLint("ScheduleExactAlarm")
    fun scheduleAlarmSafely(
        context: Context,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager is null. Cannot schedule alarm.")
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ (API 31+) - Check if we can schedule exact alarms
                if (alarmManager.canScheduleExactAlarms()) {
                    Log.d(TAG, "Exact alarm permission granted. Scheduling exact alarm...")
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    Log.w(TAG, "Exact alarm permission denied! Falling back to setAndAllowWhileIdle() as graceful fallback.")
                    // Fallback to non-exact / relaxed alarm that matches battery/doze mode allowances safely
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android 6.0 to Android 11 -> Exact alarms are allowed by default, but let's schedule with allow-while-idle
                Log.d(TAG, "Android M to R. Scheduling exact/idle-respecting alarm...")
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                // Pre-Android 6.0 -> setExact()
                Log.d(TAG, "Pre-Android M system. Scheduling exact alarm...")
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException thrown when scheduling exact alarm! Retroactively falling back.", e)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } catch (fallbackEx: Exception) {
                Log.e(TAG, "Failed entire fallback sequence.", fallbackEx)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in scheduleAlarmSafely", e)
        }
    }
}
