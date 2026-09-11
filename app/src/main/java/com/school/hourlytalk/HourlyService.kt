package com.school.hourlytalk

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

class HourlyService : Service() {
    private var speaker: Speaker? = null
    override fun onCreate() {
        super.onCreate()
        speaker = Speaker(this)
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, buildNotification())
        when (intent?.action) {
            ACTION_CHIME -> Handler(Looper.getMainLooper()).postDelayed({
                speaker?.speak(TimeHelper.speakTime(this), playChime = true) { stopSelf() }
            }, 600)
            ACTION_KEEP -> { }
            else -> stopSelf()
        }
        return START_NOT_STICKY
    }
    private fun buildNotification(): Notification {
        val pi = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, TalkApp.CHANNEL_ID)
            .setContentTitle("整点语音钟")
            .setContentText("整点将自动报时")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
    }
    override fun onDestroy() { speaker?.shutdown(); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
    companion object {
        const val ACTION_CHIME = "chime"
        const val ACTION_KEEP = "keep"
    }
}
