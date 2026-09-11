package com.school.hourlytalk

object VoiceParser {
    fun handle(text: String): IntentKind {
        val t = text.replace(" ", "").lowercase()
        return when {
            t.contains("再见") || t.contains("结束") || t.contains("退出") || t.contains("闭嘴") || t.contains("停止") ->
                IntentKind.BYE
            t.contains("天气") || t.contains("气温") || t.contains("热不热") || t.contains("冷不冷") || t.contains("下雨") ->
                IntentKind.WEATHER
            t.contains("光线") || t.contains("亮度") || t.contains("黑不黑") || t.contains("亮不亮") ->
                IntentKind.LIGHT
            t.contains("校时") || t.contains("对时") || t.contains("准不准") ->
                IntentKind.SYNC
            t.contains("换声") || t.contains("换一个声音") || t.contains("女声") || t.contains("男声") || t.contains("清亮") ->
                IntentKind.VOICE
            t.contains("你好") || t.contains("在吗") || t.contains("早上好") || t.contains("晚上好") ->
                IntentKind.HELLO
            t.contains("点") || t.contains("时间") || t.contains("几号") || t.contains("日期") || t.contains("现在") ->
                IntentKind.TIME
            else -> IntentKind.UNKNOWN
        }
    }

    enum class IntentKind { TIME, WEATHER, LIGHT, SYNC, VOICE, HELLO, BYE, UNKNOWN }
}
