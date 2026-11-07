package com.contracts.app.utils

import java.util.Calendar

object JalaliConverter {

    fun toGregorian(jalaliStr: String): Triple<Int, Int, Int>? {
        return try {
            val cleaned = jalaliStr.trim().replace("-", "/").replace(".", "/")
            val parts = cleaned.split("/")
            if (parts.size != 3) return null
            var jY = parts[0].toLong()
            val jM = parts[1].toLong()
            val jD = parts[2].toLong()
            if (jY < 1300 || jY > 1500) return null
            if (jM < 1 || jM > 12) return null
            val maxD = if (jM <= 6) 31L else if (jM <= 11) 30L else 30L
            if (jD < 1 || jD > maxD) return null

            jY -= 979
            val jM0 = jM - 1
            var jDN = 365L * jY + (jY / 33) * 8 + ((jY % 33 + 3) / 4)
            for (i in 0 until jM0) jDN += if (i < 6) 31L else 30L
            jDN += jD

            var gDN = jDN + 79
            var gY = 1600L + 400L * (gDN / 146097)
            gDN %= 146097
            var leap = 1L
            if (gDN >= 36525) {
                gDN -= 1
                gY += 100L * (gDN / 36524)
                gDN %= 36524
                if (gDN >= 365) gDN += 1 else leap = 0
            }
            gY += 4L * (gDN / 1461)
            gDN %= 1461
            if (gDN >= 366) {
                leap = 0; gDN -= 1; gY += gDN / 365; gDN %= 365
            }
            val gMD = longArrayOf(31, 28 + leap, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
            var i = 0
            while (i < 11 && gDN >= gMD[i]) { gDN -= gMD[i]; i++ }
            Triple(gY.toInt(), i + 1, (gDN + 1).toInt())
        } catch (e: Exception) { null }
    }

    fun isValid(s: String): Boolean {
        val c = s.trim().replace("-", "/").replace(".", "/")
        if (c.isEmpty() || c.toDoubleOrNull() != null) return false
        val parts = c.split("/")
        if (parts.size != 3) return false
        val yr = parts[0].toLongOrNull() ?: return false
        return yr in 1300..1500
    }

    fun daysRemaining(endJalali: String): Long {
        val end = toGregorian(endJalali) ?: return 0L
        val cal = Calendar.getInstance()
        val today = Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
        return julianDiff(today, end)
    }

    fun durationMonths(startJalali: String, endJalali: String): Int {
        val s = toGregorian(startJalali) ?: return 0
        val e = toGregorian(endJalali) ?: return 0
        return (e.first - s.first) * 12 + (e.second - s.second)
    }

    fun formatGregorian(t: Triple<Int, Int, Int>) =
        "%04d-%02d-%02d".format(t.first, t.second, t.third)

    private fun julianDiff(from: Triple<Int, Int, Int>, to: Triple<Int, Int, Int>): Long {
        fun j(y: Int, m: Int, d: Int): Long {
            val a = (14 - m) / 12; val yy = y + 4800 - a; val mm = m + 12 * a - 3
            return d + (153 * mm + 2) / 5 + 365L * yy + yy / 4 - yy / 100 + yy / 400 - 32045
        }
        return j(to.first, to.second, to.third) - j(from.first, from.second, from.third)
    }
}
