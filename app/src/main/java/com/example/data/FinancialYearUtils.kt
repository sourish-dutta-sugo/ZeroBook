package com.example.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

object FinancialYearUtils {
    private val financialYearFormatter = DateTimeFormatter.ofPattern("yyyy", Locale.ENGLISH)

    fun currentFinancialYearCode(zoneId: ZoneId = ZoneId.systemDefault()): String =
        financialYearCodeFor(Instant.now().toEpochMilli(), zoneId)

    fun financialYearCodeFor(timestamp: Long, zoneId: ZoneId = ZoneId.systemDefault()): String {
        val date = Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate()
        return financialYearCodeFor(date)
    }

    fun financialYearCodeFor(date: LocalDate): String {
        val startYear = if (date.monthValue >= 4) date.year else date.year - 1
        val endYearShort = (startYear + 1) % 100
        return String.format(Locale.ENGLISH, "%04d-%02d", startYear, endYearShort)
    }

    fun startDateFor(code: String): LocalDate {
        val startYear = code.substringBefore("-").toInt()
        return LocalDate.of(startYear, 4, 1)
    }

    fun endDateFor(code: String): LocalDate = startDateFor(code).plusYears(1).minusDays(1)

    fun startMillisFor(code: String, zoneId: ZoneId = ZoneId.systemDefault()): Long =
        startDateFor(code).atStartOfDay(zoneId).toInstant().toEpochMilli()

    fun endMillisFor(code: String, zoneId: ZoneId = ZoneId.systemDefault()): Long =
        endDateFor(code).plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1L

    fun nextFinancialYear(code: String): String {
        val nextStart = startDateFor(code).plusYears(1)
        return financialYearCodeFor(nextStart)
    }

    fun previousFinancialYear(code: String): String {
        val previousStart = startDateFor(code).minusYears(1)
        return financialYearCodeFor(previousStart)
    }

    fun buildGeneratedYears(
        centerCode: String = currentFinancialYearCode(),
        previousCount: Int = 1,
        futureCount: Int = 5
    ): List<String> {
        val years = mutableListOf<String>()
        var cursor = centerCode
        repeat(previousCount) {
            cursor = previousFinancialYear(cursor)
        }
        repeat(previousCount + futureCount + 1) {
            years += cursor
            cursor = nextFinancialYear(cursor)
        }
        return years.distinct()
    }

    fun toDisplayLabel(code: String): String = code
}
