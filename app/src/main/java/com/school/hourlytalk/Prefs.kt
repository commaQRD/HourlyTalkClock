package com.school.hourlytalk

import android.content.Context

object Prefs {
    private const val NAME = "hourly_talk"

    const val VOICE_SOFT = 0
    const val VOICE_STEADY = 1
    const val VOICE_BRIGHT = 2

    fun hourlyEnabled(ctx: Context) =
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE).getBoolean("hourly", false)

    fun setHourlyEnabled(ctx: Context, on: Boolean) {
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit().putBoolean("hourly", on).apply()
    }

    fun lastLux(ctx: Context) =
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE).getFloat("lux", 80f)

    fun setLastLux(ctx: Context, lux: Float) {
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit().putFloat("lux", lux).apply()
    }

    fun ntpOffsetMs(ctx: Context) =
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE).getLong("ntp_off", 0L)

    fun setNtpOffsetMs(ctx: Context, offset: Long) {
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .edit().putLong("ntp_off", offset).putLong("ntp_at", System.currentTimeMillis()).apply()
    }

    fun lastNtpAt(ctx: Context) =
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE).getLong("ntp_at", 0L)

    fun voiceStyle(ctx: Context) =
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE).getInt("voice", VOICE_SOFT)

    fun setVoiceStyle(ctx: Context, style: Int) {
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit().putInt("voice", style).apply()
    }

    fun voiceName(style: Int) = when (style) {
        VOICE_STEADY -> "沉稳声"
        VOICE_BRIGHT -> "清亮声"
        else -> "温柔声"
    }
}
