package com.karaketir.coachingapp.fragments

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.karaketir.coachingapp.R
import com.karaketir.coachingapp.models.Study
import com.karaketir.coachingapp.services.StatRangeScaling
import com.karaketir.coachingapp.services.StudentWeeklyGoalsPreferences
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

data class StatRingViews(
    val progress: CircularProgressIndicator,
    val value: TextView,
    val unit: TextView,
    val label: TextView,
    val subtitle: TextView,
)

data class StudentHomeStats(
    val studyMinutes: Int,
    val studyMinutesTarget: Int,
    val questions: Int,
    val questionsTarget: Int,
    val reportDays: Int,
    val reportDaysTarget: Int,
)

data class TeacherHomeStats(
    val reportingPercent: Int,
    val reportingCount: Int,
    val studentCount: Int,
    val classStudyPercent: Int,
    val classStudyMinutes: Int,
    val classStudyBenchmarkMinutes: Int,
    val ratingPercent: Int,
    val ratingCount: Int,
    val timeRangeSelected: Boolean,
)

object MainHomeStatsBinder {

    private const val TAG = "MainHomeStats"
    private const val WEEKLY_GOALS_TAG = "KTS:WeeklyGoals"
    private const val COLLAPSE_PREFS_NAME = "main_home_stats_collapse"
    private const val KEY_COLLAPSED_STUDENT = "collapsed_student"
    private const val KEY_COLLAPSED_TEACHER = "collapsed_teacher"

    enum class HomeStatsRole {
        STUDENT,
        TEACHER,
    }

    fun setupStatsCollapse(statsRoot: View, role: HomeStatsRole, showToggle: Boolean = true) {
        val toggle = statsRoot.findViewById<MaterialButton>(R.id.statsCollapseToggle) ?: return
        val content = statsRoot.findViewById<View>(R.id.statsRingsContent) ?: return
        if (!showToggle) {
            toggle.visibility = View.GONE
            content.visibility = View.VISIBLE
            return
        }

        toggle.visibility = View.VISIBLE
        val prefs = statsRoot.context.applicationContext
            .getSharedPreferences(COLLAPSE_PREFS_NAME, Context.MODE_PRIVATE)
        val key = when (role) {
            HomeStatsRole.STUDENT -> KEY_COLLAPSED_STUDENT
            HomeStatsRole.TEACHER -> KEY_COLLAPSED_TEACHER
        }
        var collapsed = prefs.getBoolean(key, false)

        fun applyState(animate: Boolean) {
            content.visibility = if (collapsed) View.GONE else View.VISIBLE
            val rotation = if (collapsed) 0f else 180f
            if (animate) {
                toggle.animate().rotation(rotation).setDuration(200L).start()
            } else {
                toggle.rotation = rotation
            }
            toggle.contentDescription = statsRoot.context.getString(
                if (collapsed) R.string.main_stats_expand else R.string.main_stats_collapse,
            )
        }

        applyState(animate = false)
        toggle.setOnClickListener {
            collapsed = !collapsed
            prefs.edit().putBoolean(key, collapsed).apply()
            applyState(animate = true)
        }
    }

    fun ringViewsFromInclude(includeRoot: View): StatRingViews = StatRingViews(
        progress = includeRoot.findViewById(R.id.statProgress),
        value = includeRoot.findViewById(R.id.statValue),
        unit = includeRoot.findViewById(R.id.statUnit),
        label = includeRoot.findViewById(R.id.statLabel),
        subtitle = includeRoot.findViewById(R.id.statSubtitle),
    )

    fun computeStudentStats(
        studies: List<Study>,
        weeklyGoalMinutes: Int,
        weeklyGoalQuestions: Int,
        weeklyGoalReportDays: Int,
        rangeDays: Double = StatRangeScaling.DAYS_PER_WEEK,
    ): StudentHomeStats {
        var minutes = 0
        var questions = 0
        val reportDays = mutableSetOf<Int>()

        for (study in studies) {
            minutes += study.studyCount.toIntOrNull() ?: 0
            questions += study.soruSayisi.toIntOrNull() ?: 0
            val cal = Calendar.getInstance()
            cal.time = study.timestamp.toDate()
            reportDays.add(cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR))
        }

        val minutesTarget = StatRangeScaling.scaleWeeklyMinutes(weeklyGoalMinutes, rangeDays)
        val questionsTarget = StatRangeScaling.scaleWeeklyQuestions(weeklyGoalQuestions, rangeDays)
        val reportTarget = StatRangeScaling.scaleWeeklyReportDays(weeklyGoalReportDays, rangeDays)

        if (weeklyGoalMinutes <= 0) {
            Log.w(TAG, "computeStudentStats: weeklyGoalMinutes=$weeklyGoalMinutes, scaled target=$minutesTarget")
        }
        if (weeklyGoalQuestions <= 0) {
            Log.w(TAG, "computeStudentStats: weeklyGoalQuestions=$weeklyGoalQuestions, scaled target=$questionsTarget")
        }

        val stats = StudentHomeStats(
            studyMinutes = minutes.coerceAtLeast(0),
            studyMinutesTarget = minutesTarget,
            questions = questions.coerceAtLeast(0),
            questionsTarget = questionsTarget,
            reportDays = reportDays.size.coerceAtMost(reportTarget),
            reportDaysTarget = reportTarget,
        )
        Log.d(
            TAG,
            "computeStudentStats: studies=${studies.size} rangeDays=$rangeDays " +
                "minutes=${stats.studyMinutes}/${stats.studyMinutesTarget} " +
                "questions=${stats.questions}/${stats.questionsTarget} " +
                "reportDays=${stats.reportDays}/${stats.reportDaysTarget}",
        )
        return stats
    }

    fun defaultWeeklyGoalMinutes(): Int = StudentWeeklyGoalsPreferences.DEFAULT_MINUTES

    fun defaultWeeklyGoalQuestions(): Int = StudentWeeklyGoalsPreferences.DEFAULT_QUESTIONS

    fun defaultWeeklyReportDaysTarget(): Int = StudentWeeklyGoalsPreferences.DEFAULT_REPORT_DAYS

    fun computeTeacherStats(
        studentCount: Int,
        reportingCount: Int,
        ratingCount: Int,
        classStudyMinutes: Int,
        weeklyGoalMinutesSum: Int,
        rangeDays: Double,
        timeRangeSelected: Boolean,
    ): TeacherHomeStats {
        if (studentCount <= 0) {
            Log.d(
                WEEKLY_GOALS_TAG,
                "computeTeacherStats: no students weeklyGoalMinutesSum=0 benchmark=0",
            )
            return TeacherHomeStats(
                reportingPercent = 0,
                reportingCount = 0,
                studentCount = 0,
                classStudyPercent = 0,
                classStudyMinutes = 0,
                classStudyBenchmarkMinutes = 0,
                ratingPercent = 0,
                ratingCount = 0,
                timeRangeSelected = timeRangeSelected,
            )
        }

        val days = rangeDays.coerceAtLeast(1.0)
        val benchmark = if (weeklyGoalMinutesSum > 0) {
            StatRangeScaling.scaleWeeklyMinutes(weeklyGoalMinutesSum, days)
        } else {
            0
        }
        Log.d(
            WEEKLY_GOALS_TAG,
            "computeTeacherStats: students=$studentCount weeklyGoalMinutesSum=$weeklyGoalMinutesSum " +
                "rangeDays=$days classStudyBenchmarkMinutes=$benchmark",
        )

        val reportingPct = StatRangeScaling.progressPercent(reportingCount, studentCount)
        val ratingPct = StatRangeScaling.progressPercent(ratingCount, studentCount)
        val classPct = StatRangeScaling.progressPercent(classStudyMinutes, benchmark)

        return TeacherHomeStats(
            reportingPercent = reportingPct,
            reportingCount = reportingCount,
            studentCount = studentCount,
            classStudyPercent = classPct,
            classStudyMinutes = classStudyMinutes,
            classStudyBenchmarkMinutes = benchmark,
            ratingPercent = ratingPct,
            ratingCount = ratingCount,
            timeRangeSelected = timeRangeSelected,
        )
    }

    fun bindStudentRings(
        studyRing: StatRingViews,
        questionRing: StatRingViews,
        reportRing: StatRingViews,
        stats: StudentHomeStats,
        resources: android.content.res.Resources,
        rangeDays: Double = StatRangeScaling.DAYS_PER_WEEK,
    ) {
        val weeklyRange = StatRangeScaling.isWeeklyRange(rangeDays)
        Log.d(TAG, "bindStudentRings: branch=student rangeDays=$rangeDays weeklyRange=$weeklyRange")
        val studyPct = StatRangeScaling.progressPercent(stats.studyMinutes, stats.studyMinutesTarget)
        bindRing(
            ringName = "study",
            branch = "student",
            views = studyRing,
            actual = stats.studyMinutes,
            target = stats.studyMinutesTarget,
            progress = studyPct,
            valueText = "$studyPct%",
            label = resources.getString(
                if (weeklyRange) R.string.main_stat_weekly_study else R.string.main_stat_period_study,
            ),
            subtitle = resources.getString(
                R.string.main_stat_study_subtitle,
                formatMinutes(stats.studyMinutes, resources),
                formatWeeklyGoalTarget(stats.studyMinutesTarget, resources),
            ),
            indicatorColorRes = R.color.stat_ring_study,
            trackColorRes = R.color.stat_track_study,
        )

        val questionPct = StatRangeScaling.progressPercent(stats.questions, stats.questionsTarget)
        bindRing(
            ringName = "questions",
            branch = "student",
            views = questionRing,
            actual = stats.questions,
            target = stats.questionsTarget,
            progress = questionPct,
            valueText = "$questionPct%",
            label = resources.getString(
                if (weeklyRange) R.string.main_stat_weekly_questions else R.string.main_stat_period_questions,
            ),
            subtitle = resources.getString(
                R.string.main_stat_study_subtitle,
                stats.questions.toString(),
                stats.questionsTarget.toString(),
            ),
            indicatorColorRes = R.color.stat_ring_questions,
            trackColorRes = R.color.stat_track_questions,
        )

        val reportPct = StatRangeScaling.progressPercent(stats.reportDays, stats.reportDaysTarget)
        bindRing(
            ringName = "reportDays",
            branch = "student",
            views = reportRing,
            actual = stats.reportDays,
            target = stats.reportDaysTarget,
            progress = reportPct,
            valueText = "$reportPct%",
            label = resources.getString(
                if (weeklyRange) R.string.main_stat_report_days else R.string.main_stat_period_report_days,
            ),
            subtitle = resources.getString(
                R.string.main_stat_days_subtitle,
                stats.reportDays,
                stats.reportDaysTarget,
            ),
            indicatorColorRes = R.color.stat_ring_report,
            trackColorRes = R.color.stat_track_report,
        )
    }

    fun bindTeacherRings(
        reportingRing: StatRingViews,
        classRing: StatRingViews,
        ratingRing: StatRingViews,
        stats: TeacherHomeStats,
        resources: android.content.res.Resources,
    ) {
        if (!stats.timeRangeSelected) {
            Log.d(TAG, "bindTeacherRings: branch=teacher noTimeRangeSelected")
            val hint = resources.getString(R.string.main_stat_select_time_range)
            bindRing(
                ringName = "reporting",
                branch = "teacher/noTimeRange",
                views = reportingRing,
                actual = null,
                target = null,
                progress = 0,
                valueText = "—",
                label = resources.getString(R.string.main_stat_report_rate),
                subtitle = hint,
                indicatorColorRes = R.color.stat_ring_teacher_reporting,
                trackColorRes = R.color.stat_track_teacher_reporting,
            )
            bindRing(
                ringName = "classStudy",
                branch = "teacher/noTimeRange",
                views = classRing,
                actual = null,
                target = null,
                progress = 0,
                valueText = "—",
                label = resources.getString(R.string.main_stat_class_study),
                subtitle = hint,
                indicatorColorRes = R.color.stat_ring_teacher_class,
                trackColorRes = R.color.stat_track_teacher_class,
            )
            bindRing(
                ringName = "rating",
                branch = "teacher/noTimeRange",
                views = ratingRing,
                actual = null,
                target = null,
                progress = 0,
                valueText = "—",
                label = resources.getString(R.string.main_stat_rating_rate),
                subtitle = hint,
                indicatorColorRes = R.color.stat_ring_teacher_rating,
                trackColorRes = R.color.stat_track_teacher_rating,
            )
            return
        }

        if (stats.studentCount <= 0) {
            Log.w(TAG, "bindTeacherRings: branch=teacher noStudents studentCount=0")
            val empty = resources.getString(R.string.main_stat_no_students)
            bindRing(
                ringName = "reporting",
                branch = "teacher/noStudents",
                views = reportingRing,
                actual = 0,
                target = stats.studentCount,
                progress = 0,
                valueText = "0%",
                label = resources.getString(R.string.main_stat_report_rate),
                subtitle = empty,
                indicatorColorRes = R.color.stat_ring_teacher_reporting,
                trackColorRes = R.color.stat_track_teacher_reporting,
            )
            bindRing(
                ringName = "classStudy",
                branch = "teacher/noStudents",
                views = classRing,
                actual = 0,
                target = stats.classStudyBenchmarkMinutes,
                progress = 0,
                valueText = "0%",
                label = resources.getString(R.string.main_stat_class_study),
                subtitle = empty,
                indicatorColorRes = R.color.stat_ring_teacher_class,
                trackColorRes = R.color.stat_track_teacher_class,
            )
            bindRing(
                ringName = "rating",
                branch = "teacher/noStudents",
                views = ratingRing,
                actual = 0,
                target = stats.studentCount,
                progress = 0,
                valueText = "0%",
                label = resources.getString(R.string.main_stat_rating_rate),
                subtitle = empty,
                indicatorColorRes = R.color.stat_ring_teacher_rating,
                trackColorRes = R.color.stat_track_teacher_rating,
            )
            return
        }

        Log.d(
            TAG,
            "bindTeacherRings: branch=teacher students=${stats.studentCount} " +
                "reporting=${stats.reportingCount}/${stats.studentCount} " +
                "classMinutes=${stats.classStudyMinutes}/${stats.classStudyBenchmarkMinutes} " +
                "rating=${stats.ratingCount}/${stats.studentCount}",
        )

        val subtitleStudents = resources.getString(
            R.string.main_stat_students_reporting,
            stats.reportingCount,
            stats.studentCount,
        )

        bindRing(
            ringName = "reporting",
            branch = "teacher",
            views = reportingRing,
            actual = stats.reportingCount,
            target = stats.studentCount,
            progress = stats.reportingPercent,
            valueText = "${stats.reportingPercent}%",
            label = resources.getString(R.string.main_stat_report_rate),
            subtitle = subtitleStudents,
            indicatorColorRes = R.color.stat_ring_teacher_reporting,
            trackColorRes = R.color.stat_track_teacher_reporting,
        )

        bindRing(
            ringName = "classStudy",
            branch = "teacher",
            views = classRing,
            actual = stats.classStudyMinutes,
            target = stats.classStudyBenchmarkMinutes,
            progress = stats.classStudyPercent,
            valueText = "${stats.classStudyPercent}%",
            label = resources.getString(R.string.main_stat_class_study),
            subtitle = resources.getString(
                R.string.main_stat_study_subtitle,
                formatMinutes(stats.classStudyMinutes, resources),
                formatMinutes(stats.classStudyBenchmarkMinutes, resources),
            ),
            indicatorColorRes = R.color.stat_ring_teacher_class,
            trackColorRes = R.color.stat_track_teacher_class,
        )

        bindRing(
            ringName = "rating",
            branch = "teacher",
            views = ratingRing,
            actual = stats.ratingCount,
            target = stats.studentCount,
            progress = stats.ratingPercent,
            valueText = "${stats.ratingPercent}%",
            label = resources.getString(R.string.main_stat_rating_rate),
            subtitle = resources.getString(
                R.string.main_stat_students_reporting,
                stats.ratingCount,
                stats.studentCount,
            ),
            indicatorColorRes = R.color.stat_ring_teacher_rating,
            trackColorRes = R.color.stat_track_teacher_rating,
        )
    }

    private fun bindRing(
        ringName: String,
        branch: String,
        views: StatRingViews,
        actual: Int?,
        target: Int?,
        progress: Int,
        valueText: String,
        label: String,
        subtitle: String,
        indicatorColorRes: Int,
        trackColorRes: Int,
    ) {
        val clampedProgress = progress.coerceIn(0, 100)
        if (target != null && target <= 0) {
            Log.w(
                TAG,
                "bindRing[$ringName]: branch=$branch actual=$actual target=$target progress=$clampedProgress (zero target)",
            )
        } else {
            Log.d(
                TAG,
                "bindRing[$ringName]: branch=$branch actual=$actual target=$target " +
                    "progress=$clampedProgress valueText=$valueText " +
                    "colors=indicator@$indicatorColorRes track@$trackColorRes",
            )
        }
        val context = views.progress.context
        views.progress.max = 100
        views.progress.setProgressCompat(clampedProgress, true)
        views.progress.setIndicatorColor(ContextCompat.getColor(context, indicatorColorRes))
        views.progress.trackColor = ContextCompat.getColor(context, trackColorRes)
        views.value.text = valueText
        views.unit.visibility = View.GONE
        views.label.text = label
        views.subtitle.text = subtitle
    }

    private fun formatWeeklyGoalTarget(minutes: Int, resources: android.content.res.Resources): String {
        if (minutes >= 60 && minutes % 60 == 0) {
            return "${minutes / 60} ${resources.getString(R.string.main_stat_hours_short)}"
        }
        return formatMinutes(minutes, resources)
    }

    private fun formatMinutes(minutes: Int, resources: android.content.res.Resources): String {
        if (minutes < 60) {
            return "$minutes ${resources.getString(R.string.main_stat_minutes_short)}"
        }
        val hours = minutes / 60
        val rem = minutes % 60
        return if (rem == 0) {
            "$hours ${resources.getString(R.string.main_stat_hours_short)}"
        } else {
            String.format(
                Locale.getDefault(),
                "%d %s %d %s",
                hours,
                resources.getString(R.string.main_stat_hours_short),
                rem,
                resources.getString(R.string.main_stat_minutes_short),
            )
        }
    }
}
