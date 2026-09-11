package com.school.hourlytalk

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

object WeatherClient {
    private const val LAT = "22.3193"
    private const val LON = "114.1694"
    fun fetchAsync(onDone: (String) -> Unit) {
        thread {
            try {
                val url = "https://api.open-meteo.com/v1/forecast?latitude=$LAT&longitude=$LON&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=Asia%2FHong_Kong"
                val text = httpGet(url)
                val cur = JSONObject(text).getJSONObject("current")
                val temp = cur.getDouble("temperature_2m")
                val hum = cur.optInt("relative_humidity_2m", -1)
                val wind = cur.optDouble("wind_speed_10m", 0.0)
                val code = cur.optInt("weather_code", 0)
                val speak = "香港现在${weatherText(code)}，气温${temp.toInt()}度" +
                    (if (hum >= 0) "，湿度百分之$hum" else "") +
                    "，风速每小时${wind.toInt()}公里。"
                onDone(speak)
            } catch (e: Exception) {
                onDone("天气获取失败，请检查网络。")
            }
        }
    }
    private fun weatherText(code: Int): String = when (code) {
        0 -> "晴"; 1, 2 -> "少云"; 3 -> "阴天"; 45, 48 -> "有雾"
        51, 53, 55 -> "毛毛雨"; 61, 63, 65 -> "下雨"
        71, 73, 75 -> "下雪"; 80, 81, 82 -> "阵雨"
        95, 96, 99 -> "有雷暴"; else -> "天气正常"
    }
    private fun httpGet(url: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 8000; conn.readTimeout = 8000; conn.requestMethod = "GET"
        conn.inputStream.bufferedReader().use { return it.readText() }
    }
}
