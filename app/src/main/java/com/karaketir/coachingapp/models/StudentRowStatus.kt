package com.karaketir.coachingapp.models

import java.util.Date

/** Öğretmen ana ekranındaki öğrenci satırı için önceden yüklenmiş özet veriler. */
data class StudentRowStatus(
    val hasStudyInRange: Boolean = false,
    val ratingStars: Int? = null,
    val ratingDate: Date? = null,
    val reportTimestamp: Date? = null,
)
