package com.karaketir.coachingapp.services

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StudentWeeklyGoalsPreferencesTest {

    @Test
    fun validateHours_withinRange() {
        assertTrue(StudentWeeklyGoalsPreferences.validateHours(7))
        assertFalse(StudentWeeklyGoalsPreferences.validateHours(0))
        assertFalse(StudentWeeklyGoalsPreferences.validateHours(41))
    }

    @Test
    fun hoursToMinutes_convertsWholeHours() {
        assertEquals(420, StudentWeeklyGoalsPreferences.hoursToMinutes(7))
        assertEquals(2400, StudentWeeklyGoalsPreferences.hoursToMinutes(40))
    }

    @Test
    fun minutesToDisplayHours_roundsLegacyValues() {
        assertEquals(7, StudentWeeklyGoalsPreferences.minutesToDisplayHours(420))
        assertEquals(1, StudentWeeklyGoalsPreferences.minutesToDisplayHours(45))
    }

    @Test
    fun clampReportDays_toValidRange() {
        assertEquals(7, StudentWeeklyGoalsPreferences.clampReportDays(99))
        assertEquals(1, StudentWeeklyGoalsPreferences.clampReportDays(0))
    }

    @Test
    fun validateQuestions_withinRange() {
        assertTrue(StudentWeeklyGoalsPreferences.validateQuestions(350))
        assertTrue(StudentWeeklyGoalsPreferences.validateQuestions(9999))
        assertFalse(StudentWeeklyGoalsPreferences.validateQuestions(9))
        assertFalse(StudentWeeklyGoalsPreferences.validateQuestions(3001))
    }
}
