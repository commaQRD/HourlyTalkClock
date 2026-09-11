package com.school.hourlytalk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class HourlyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (!Prefs.hourlyEnabled(context)) return
        context.startForegroundService(Intent(context, HourlyService::class.java).apply {
            action = HourlyService.ACTION_CHIME
        })
        AlarmScheduler.scheduleNextHour(context)
    }
}
