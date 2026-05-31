package com.karaketir.coachingapp.services

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Pro-rates weekly student goals to a selected calendar range so progress rings
 * compare actuals in [rangeDays] against a fair target for that same period.
 */
object StatRangeScaling {

    const val DAYS_PER_WEEK = 7.0

    /** True when the range is effectively one calendar week (student home default). */
    fun isWeeklyRange(rangeDays: Double): Boolean =
        abs(rangeDays - DAYS_PER_WEEK) < 0.5

    fun scaleWeeklyMinutes(weeklyMinutes: Int, rangeDays: Double): Int {
        val days = rangeDays.coerceAtLeast(1.0)
        if (weeklyMinutes <= 0) return 1
        return (weeklyMinutes * days / DAYS_PER_WEEK).roundToInt().coerceAtLeast(1)
    }

    fun scaleWeeklyQuestions(weeklyQuestions: Int, rangeDays: Double): Int {
        val days = rangeDays.coerceAtLeast(1.0)
        if (weeklyQuestions <= 0) return 1
        return (weeklyQuestions * days / DAYS_PER_WEEK).roundToInt().coerceAtLeast(1)
    }

    /**
     * Report-day targets cannot exceed the number of distinct calendar days in the range.
     */
    fun scaleWeeklyReportDays(weeklyReportDays: Int, rangeDays: Double): Int {
        val days = rangeDays.coerceAtLeast(1.0)
        val maxDistinctDays = days.roundToInt().coerceAtLeast(1)
        val weekly = weeklyReportDays.coerceIn(
            StudentWeeklyGoalsPreferences.MIN_REPORT_DAYS,
            StudentWeeklyGoalsPreferences.MAX_REPORT_DAYS,
        )
        if (weekly <= 0) return 1
        val scaled = (weekly * days / DAYS_PER_WEEK).roundToInt().coerceAtLeast(1)
        return minOf(scaled, maxDistinctDays)
    }

    fun progressPercent(actual: Int, target: Int): Int {
        if (target <= 0) return 0
        return (actual * 100f / target).roundToInt().coerceIn(0, 100)
    }
}
