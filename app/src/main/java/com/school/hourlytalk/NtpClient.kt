package com.school.hourlytalk

import android.content.Context
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.concurrent.thread

object NtpClient {
    fun syncAsync(ctx: Context, onDone: (Boolean, String) -> Unit) {
        thread {
            val hosts = listOf("ntp.aliyun.com", "time.apple.com", "time.google.com")
            for (host in hosts) {
                try {
                    val offset = query(host)
                    Prefs.setNtpOffsetMs(ctx, offset)
                    onDone(true, "校时成功 $host，偏差 ${offset}ms")
                    return@thread
                } catch (_: Exception) {
                }
            }
            onDone(false, "校时失败，使用手机本地时间")
        }
    }

    private fun query(host: String): Long {
        val buf = ByteArray(48)
        buf[0] = 0b00_100_011.toByte()
        DatagramSocket().use { socket ->
            socket.soTimeout = 3000
            val address = InetAddress.getByName(host)
            socket.send(DatagramPacket(buf, buf.size, address, 123))
            val incoming = DatagramPacket(buf, buf.size)
            val t1 = System.currentTimeMillis()
            socket.receive(incoming)
            val t4 = System.currentTimeMillis()
            val seconds = readInt(buf, 40)
            val fraction = readInt(buf, 44)
            val ntpTime = ((seconds - 2208988800L) * 1000L) + (fraction * 1000L / 0x100000000L)
            return ntpTime - ((t1 + t4) / 2)
        }
    }

    private fun readInt(buf: ByteArray, offset: Int): Long {
        var v = 0L
        for (i in 0..3) v = (v shl 8) or (buf[offset + i].toInt() and 0xFF).toLong()
        return v
    }
}
