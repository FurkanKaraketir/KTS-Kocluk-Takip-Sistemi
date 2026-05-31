package com.karaketir.coachingapp.services

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StatRangeScalingTest {

    @Test
    fun isWeeklyRange_nearSevenDays() {
        assertTrue(StatRangeScaling.isWeeklyRange(7.0))
        assertTrue(StatRangeScaling.isWeeklyRange(6.8))
        assertFalse(StatRangeScaling.isWeeklyRange(1.0))
        assertFalse(StatRangeScaling.isWeeklyRange(30.0))
    }

    @Test
    fun scaleWeeklyMinutes_oneDay_proratesFromWeekly() {
        assertEquals(60, StatRangeScaling.scaleWeeklyMinutes(420, 1.0))
    }

    @Test
    fun scaleWeeklyMinutes_fullWeek_unchanged() {
        assertEquals(420, StatRangeScaling.scaleWeeklyMinutes(420, 7.0))
    }

    @Test
    fun scaleWeeklyMinutes_thirtyDays_scalesUp() {
        assertEquals(1800, StatRangeScaling.scaleWeeklyMinutes(420, 30.0))
    }

    @Test
    fun scaleWeeklyMinutes_zeroWeeklyGoal_returnsMinimumTarget() {
        assertEquals(1, StatRangeScaling.scaleWeeklyMinutes(0, 7.0))
    }

    @Test
    fun scaleWeeklyReportDays_oneDay_cappedAtOne() {
        assertEquals(1, StatRangeScaling.scaleWeeklyReportDays(7, 1.0))
    }

    @Test
    fun scaleWeeklyReportDays_threeDays_prorates() {
        assertEquals(3, StatRangeScaling.scaleWeeklyReportDays(7, 3.0))
    }

    @Test
    fun progressPercent_capsAt100() {
        assertEquals(100, StatRangeScaling.progressPercent(9999, 100))
    }

    @Test
    fun progressPercent_zeroTarget_returnsZero() {
        assertEquals(0, StatRangeScaling.progressPercent(50, 0))
    }

    @Test
    fun progressPercent_zeroActual_returnsZero() {
        assertEquals(0, StatRangeScaling.progressPercent(0, 420))
    }

    @Test
    fun oneDayRange_matchesActualsAt100Percent() {
        val minutesTarget = StatRangeScaling.scaleWeeklyMinutes(420, 1.0)
        val questionsTarget = StatRangeScaling.scaleWeeklyQuestions(350, 1.0)
        assertEquals(60, minutesTarget)
        assertEquals(50, questionsTarget)
        assertEquals(100, StatRangeScaling.progressPercent(60, minutesTarget))
        assertEquals(100, StatRangeScaling.progressPercent(50, questionsTarget))
    }

    @Test
    fun fullWeekRange_keepsWeeklyTargets() {
        assertEquals(420, StatRangeScaling.scaleWeeklyMinutes(420, 7.0))
        assertEquals(350, StatRangeScaling.scaleWeeklyQuestions(350, 7.0))
        assertEquals(5, StatRangeScaling.scaleWeeklyReportDays(5, 7.0))
    }

    @Test
    fun teacherClassBenchmark_oneDay_scalesSumOfStudentWeeklyGoals() {
        val benchmark = StatRangeScaling.scaleWeeklyMinutes(700, 1.0)
        assertEquals(100, benchmark)
        assertEquals(100, StatRangeScaling.progressPercent(300, benchmark))
    }
}
