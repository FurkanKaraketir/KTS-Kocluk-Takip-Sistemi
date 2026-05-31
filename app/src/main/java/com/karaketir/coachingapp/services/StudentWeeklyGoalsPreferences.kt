package com.karaketir.coachingapp.services

import android.content.Context
import android.util.Log
import kotlin.math.roundToInt

/**
 * Student-configured weekly targets for the main home stat rings.
 * Per-subject teacher goals in [HaftalikHedefler] are shown on the goals screen only.
 */
class StudentWeeklyGoalsPreferences(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getMinutes(): Int {
        val raw = prefs.getInt(KEY_MINUTES, DEFAULT_MINUTES)
        val result = raw.coerceIn(MIN_MINUTES, MAX_MINUTES)
        Log.d(TAG, "prefs.getMinutes: raw=$raw coerced=$result default=$DEFAULT_MINUTES")
        if (raw != result) {
            Log.w(TAG, "prefs.getMinutes: clamped from $raw to $result")
        }
        return result
    }

    /** Whole hours for settings UI (legacy minute values are rounded up). */
    fun getStudyHours(): Int = minutesToDisplayHours(getMinutes())

    fun getQuestions(): Int {
        val raw = prefs.getInt(KEY_QUESTIONS, DEFAULT_QUESTIONS)
        val result = raw.coerceIn(MIN_QUESTIONS, MAX_QUESTIONS)
        Log.d(TAG, "prefs.getQuestions: raw=$raw coerced=$result default=$DEFAULT_QUESTIONS")
        if (raw != result) {
            Log.w(TAG, "prefs.getQuestions: clamped from $raw to $result")
        }
        return result
    }

    fun getReportDays(): Int {
        val raw = prefs.getInt(KEY_REPORT_DAYS, DEFAULT_REPORT_DAYS)
        val result = raw.coerceIn(MIN_REPORT_DAYS, MAX_REPORT_DAYS)
        Log.d(TAG, "prefs.getReportDays: raw=$raw coerced=$result default=$DEFAULT_REPORT_DAYS")
        if (raw != result) {
            Log.w(TAG, "prefs.getReportDays: clamped from $raw to $result")
        }
        return result
    }

    fun save(minutes: Int, questions: Int, reportDays: Int) {
        val clampedMinutes = minutes.coerceIn(MIN_MINUTES, MAX_MINUTES)
        val clampedQuestions = questions.coerceIn(MIN_QUESTIONS, MAX_QUESTIONS)
        val clampedReportDays = reportDays.coerceIn(MIN_REPORT_DAYS, MAX_REPORT_DAYS)
        Log.d(
            TAG,
            "prefs.save: minutes=$clampedMinutes questions=$clampedQuestions reportDays=$clampedReportDays",
        )
        if (minutes != clampedMinutes || questions != clampedQuestions || reportDays != clampedReportDays) {
            Log.w(
                TAG,
                "prefs.save: input clamped from minutes=$minutes questions=$questions reportDays=$reportDays",
            )
        }
        prefs.edit()
            .putInt(KEY_MINUTES, clampedMinutes)
            .putInt(KEY_QUESTIONS, clampedQuestions)
            .putInt(KEY_REPORT_DAYS, clampedReportDays)
            .apply()
    }

    fun saveStudyHours(hours: Int, questions: Int, reportDays: Int) {
        save(hoursToMinutes(hours), questions, reportDays)
    }

    fun firestoreSyncFields(minutes: Int, questions: Int, reportDays: Int): Map<String, Int> = mapOf(
        StudyQueryHelper.FIELD_WEEKLY_GOAL_MINUTES to clampMinutes(minutes),
        StudyQueryHelper.FIELD_WEEKLY_GOAL_QUESTIONS to clampQuestions(questions),
        StudyQueryHelper.FIELD_WEEKLY_GOAL_REPORT_DAYS to clampReportDays(reportDays),
    )

    companion object {
        private const val TAG = "KTS:WeeklyGoals"

        private const val PREFS_NAME = "student_weekly_goals"

        private const val KEY_MINUTES = "weekly_minutes"
        private const val KEY_QUESTIONS = "weekly_questions"
        private const val KEY_REPORT_DAYS = "report_days"

        const val DEFAULT_MINUTES = 420
        const val DEFAULT_QUESTIONS = 350
        const val DEFAULT_REPORT_DAYS = 7

        const val MIN_HOURS = 1
        const val MAX_HOURS = 99

        const val MIN_MINUTES = MIN_HOURS * 60
        const val MAX_MINUTES = MAX_HOURS * 60
        const val MIN_QUESTIONS = 10
        const val MAX_QUESTIONS = 9999
        const val MIN_REPORT_DAYS = 1
        const val MAX_REPORT_DAYS = 7

        fun parseIntField(raw: String): Int? = raw.trim().toIntOrNull()

        fun validateHours(value: Int): Boolean = value in MIN_HOURS..MAX_HOURS

        fun validateMinutes(value: Int): Boolean = value in MIN_MINUTES..MAX_MINUTES

        fun validateQuestions(value: Int): Boolean = value in MIN_QUESTIONS..MAX_QUESTIONS

        fun validateReportDays(value: Int): Boolean = value in MIN_REPORT_DAYS..MAX_REPORT_DAYS

        fun hoursToMinutes(hours: Int): Int =
            (hours.coerceIn(MIN_HOURS, MAX_HOURS) * 60).coerceIn(MIN_MINUTES, MAX_MINUTES)

        fun minutesToDisplayHours(minutes: Int): Int =
            ((minutes.coerceIn(MIN_MINUTES, MAX_MINUTES) + 59) / 60).coerceIn(MIN_HOURS, MAX_HOURS)

        fun clampMinutes(value: Int): Int = value.coerceIn(MIN_MINUTES, MAX_MINUTES)

        fun clampQuestions(value: Int): Int = value.coerceIn(MIN_QUESTIONS, MAX_QUESTIONS)

        fun clampReportDays(value: Int): Int = value.coerceIn(MIN_REPORT_DAYS, MAX_REPORT_DAYS)

        fun suggestedWeeklyMinutesFromDaily(dailyMinutes: Int): Int =
            (dailyMinutes * 7f).roundToInt().coerceIn(MIN_MINUTES, MAX_MINUTES)
    }
}
