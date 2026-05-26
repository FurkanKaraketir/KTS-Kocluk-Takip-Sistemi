package com.karaketir.coachingapp.curriculum

object LessonPaths {
    const val LEGACY_LESSONS = "Lessons"
    const val TYMM_LESSONS = "LessonsTYMM"

    fun legacyTopicsCollection(dersAdi: String, examType: String) =
        "$LEGACY_LESSONS/$dersAdi/$examType"

    fun tymmThemesCollection(dersAdi: String, sinif: Int) =
        "$TYMM_LESSONS/$dersAdi/$sinif"
}
