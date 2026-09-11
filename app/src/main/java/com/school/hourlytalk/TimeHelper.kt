package com.school.hourlytalk

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TimeHelper {
    fun nowMillis(ctx: Context): Long = System.currentTimeMillis() + Prefs.ntpOffsetMs(ctx)

    fun clockText(ctx: Context): String =
        SimpleDateFormat("HH:mm:ss", Locale.CHINA).format(Date(nowMillis(ctx)))

    fun dateText(ctx: Context): String =
        SimpleDateFormat("yyyy年M月d日 EEEE", Locale.CHINA).format(Date(nowMillis(ctx)))

    fun speakTime(ctx: Context): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = nowMillis(ctx)
        val h = cal.get(Calendar.HOUR_OF_DAY)
        val m = cal.get(Calendar.MINUTE)
        val period = when (h) {
            in 0..5 -> "凌晨"
            in 6..11 -> "上午"
            12 -> "中午"
            in 13..17 -> "下午"
            else -> "晚上"
        }
        val h12 = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        return if (m == 0) "现在是${period}${h12}点整" else "现在是${period}${h12}点${m}分"
    }

    fun volumeFromLux(lux: Float): Float = when {
        lux < 5f -> 0.15f
        lux < 20f -> 0.35f
        lux < 80f -> 0.6f
        else -> 1.0f
    }
}
