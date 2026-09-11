package com.school.hourlytalk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        if (Prefs.hourlyEnabled(context)) {
            AlarmScheduler.scheduleNextHour(context)
            context.startForegroundService(Intent(context, HourlyService::class.java).apply {
                action = HourlyService.ACTION_KEEP
            })
        }
    }
}
